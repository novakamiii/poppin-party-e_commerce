package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.CartItemDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderStatusDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class OrdersController {
        private static final Logger log = LoggerFactory.getLogger(OrdersController.class);
        // === ORDER MANAGEMENT ===
        @Autowired
        private OrderItemRepository orderItemRepository;
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private OrderRepository orderRepository;
        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private NotificationService notificationService;

        @GetMapping("/order/status")
        public String viewOrderStatus() {
                return "orderstatus";
        }

        @GetMapping("/order/checkout")
        public String showCheckout(Model model, Principal principal) {
                User user = userRepository.findByUsername(principal.getName()).orElseThrow();
                model.addAttribute("address", user.getAddress());
                return "checkout";
        }

        @GetMapping("/order/success")
        public String orderSuccess() {
                return "ordersuccess";
        }

        @PostMapping("/order/buy-now")
        @ResponseBody
        public ResponseEntity<?> buyNow(
                        @RequestParam Long productId,
                        @RequestParam int quantity,
                        Principal principal) {

                try {
                        User user = userRepository.findByUsername(principal.getName())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        if (product.getStock() == 0) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(Map.of("error", "Buy now failed: This product is out of stock!"));
                        } else if (product.getStock() < quantity) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(Map.of("error", "Buy now failed: Only " + product.getStock()
                                                                + " items available!"));
                        }

                        // Do NOT save to cart/order_items table — just return DTO
                        CartItemDTO dto = new CartItemDTO();
                        dto.setProductId(product.getId());
                        dto.setItemName(product.getItemName());
                        dto.setImageLoc(product.getImageLoc());
                        dto.setQuantity(quantity);
                        dto.setUnitPrice(product.getPrice());
                        dto.setCustom(false); // optional

                        return ResponseEntity.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(dto);

                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @Transactional
        @PostMapping("/order/place")
        public String placeOrder(
                        @RequestParam String shippingOption,
                        @RequestParam String paymentMethod,
                        @RequestParam String itemsJson,
                        Principal principal,
                        RedirectAttributes redirectAttributes) {

                User user = null;
                try {
                        // 1. Get user
                        user = userRepository.findByUsername(principal.getName())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        // 2. Parse items
                        ObjectMapper mapper = new ObjectMapper();
                        List<CartItemDTO> items = mapper.readValue(itemsJson, new TypeReference<>() {
                        });

                        // 3. Validate items
                        if (items.isEmpty()) {
                                redirectAttributes.addFlashAttribute("error", "No items selected for checkout");
                                return "redirect:/cart";
                        }

                        // 4. Validate stock and calculate subtotal
                        BigDecimal subtotal = BigDecimal.ZERO;
                        for (CartItemDTO item : items) {
                                Long productId = item.getProductId();
                                int quantity = item.getQuantity();

                                if (!isCustomProduct(productId)) {
                                        Product product = productRepository.findById(productId)
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                                        if (product.getStock() < quantity) {
                                                redirectAttributes.addFlashAttribute("error",
                                                                "Insufficient stock for " + product.getItemName() +
                                                                                ". Only " + product.getStock()
                                                                                + " left.");
                                                return "redirect:/cart";
                                        }
                                }
                                subtotal = subtotal.add(
                                                BigDecimal.valueOf(item.getUnitPrice())
                                                                .multiply(BigDecimal.valueOf(quantity)));
                        }

                        // 5. Calculate totals
                        BigDecimal shippingFee = switch (shippingOption) {
                                case "express" -> BigDecimal.valueOf(75);
                                case "overnight" -> BigDecimal.valueOf(150);
                                default -> BigDecimal.valueOf(45);
                        };
                        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.12));
                        BigDecimal total = subtotal.add(shippingFee).add(tax);

                        // 6. Create and save order
                        Order order = new Order();
                        order.setUser(user); // Set the user relationship properly
                        order.setTotalAmount(total);
                        order.setPaymentMethod(paymentMethod);
                        order.setShippingOption(shippingOption);
                        order.setShippingAddress(user.getAddress());
                        order.setStatus("PENDING");
                        order.setTrackingNumber(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
                        order = orderRepository.save(order); // Save and get managed entity
                        log.info("Received payment method: {}", paymentMethod);

                        // 7. Process payments and update stock
                        for (CartItemDTO item : items) {
                                Long productId = item.getProductId();
                                int quantity = item.getQuantity();
                                BigDecimal price = BigDecimal.valueOf(item.getUnitPrice());
                                String itemName = item.getItemName();

                                // Update stock for non-custom products
                                if (!isCustomProduct(productId)) {
                                        Product product = productRepository.findById(productId)
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));
                                        product.setStock(product.getStock() - quantity);
                                        productRepository.save(product);
                                }

                                // Create payment record
                                Payment payment = new Payment();
                                payment.setUser(user);
                                payment.setOrder(order);
                                payment.setItemName(itemName);
                                payment.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
                                payment.setTransactionId(order.getTrackingNumber());
                                payment.setStatus("PENDING");
                                payment.setShippingOption(shippingOption);
                                payment.setPaymentMethodDetails(paymentMethod);
                                payment.setDaysLeft(5);
                                payment.setQuantity(quantity);

                                if (isCustomProduct(productId)) {
                                        payment.setProductId(1L); // Default product ID for custom items
                                        payment.setIsCustom(true);
                                        payment.setCustomProductRef(itemName);
                                } else {
                                        payment.setProductId(productId);
                                        payment.setIsCustom(false);
                                }

                                paymentRepository.save(payment);
                        }

                        // 8. Clear cart and send notification
                        //orderItemRepository.deleteByUserId(user.getId());

                        notificationService.createNotification(
                                        user,
                                        "Your order #" + order.getId() + " has been placed",
                                        order.getTrackingNumber(),
                                        "PENDING",
                                        order.getId());

                        redirectAttributes.addFlashAttribute("trackingNumber", order.getTrackingNumber());
                        return "redirect:/order/success";

                } catch (JsonProcessingException e) {
                        log.error("JSON parsing error", e);
                        redirectAttributes.addFlashAttribute("error", "Invalid cart data format");
                } catch (RuntimeException e) {
                        log.error("Order processing error", e);
                        redirectAttributes.addFlashAttribute("error", "Order failed: " + e.getMessage());
                } catch (Exception e) {
                        log.error("Unexpected error during order placement", e);
                        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred");
                }

                return "redirect:/cart";
        }

        private boolean isCustomProduct(Long productId) {
                return productId == null || productId == -1L;
        }

        private String safeGet(Map<String, Object> item, String key) {
                Object val = item.get(key);
                if (val == null) {
                        throw new RuntimeException("Missing key '" + key + "' in item: " + item);
                }
                return val.toString();
        }

        @GetMapping("/api/orders")
        @ResponseBody
        public List<OrderStatusDTO> getOrdersByStatus(
                        @RequestParam String status,
                        Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                System.out.println("Looking for payments for user ID: " + user.getId() + ", status: " + status);

                return paymentRepository.findByUserIdAndStatus(user.getId(), status).stream()
                                .map(payment -> {
                                        Optional<Product> productOpt = productRepository
                                                        .findByItemName(payment.getItemName());
                                        String imageLoc = productOpt.map(Product::getImageLoc)
                                                        .orElse("https://placehold.co/150x100/b944fd/ffffff?font=poppins&text=Tarpulin");

                                        Long orderId = payment.getOrder() != null ? payment.getOrder().getId() : null;

                                        return new OrderStatusDTO(
                                                        payment.getId(),
                                                        payment.getItemName(),
                                                        payment.getAmount(),
                                                        imageLoc,
                                                        payment.getDaysLeft(),
                                                        payment.getShippingOption(),
                                                        payment.getStatus(),
                                                        payment.getTransactionId(),
                                                        orderId,
                                                        payment.getQuantity());
                                })
                                .toList();
        }

        @Transactional
        @PostMapping("/api/orders/cancel/{paymentId}")
        @ResponseBody
        public ResponseEntity<?> cancelOrder(@PathVariable Long paymentId, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                // Authorization check
                if (!payment.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
                }

                // Status validation
                if (!"PENDING".equals(payment.getStatus())) {
                        return ResponseEntity.badRequest()
                                        .body("Only PENDING orders can be cancelled");
                }

                // Stock restoration for non-custom products
                if (payment.getProductId() != null && !payment.getIsCustom()) {
                        Product product = productRepository.findById(payment.getProductId())
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        product.setStock(product.getStock() + payment.getQuantity());
                        productRepository.save(product);
                }

                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);

                return ResponseEntity.ok("Order cancelled successfully");
        }

        @Transactional
        @PostMapping("/api/orders/restore/{paymentId}")
        @ResponseBody
        public ResponseEntity<?> restoreOrder(@PathVariable Long paymentId, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
                if (optionalPayment.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body("Payment not found or already removed.");
                }

                Payment payment = optionalPayment.get();

                if (!payment.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
                }

                if (!"CANCELLED".equals(payment.getStatus())) {
                        return ResponseEntity.badRequest().body("Only cancelled items can be restored.");
                }

                // Stock deduction for non-custom products
                if (payment.getProductId() != null && !payment.getIsCustom()) {
                        Product product = productRepository.findById(payment.getProductId())
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        if (product.getStock() < payment.getQuantity()) {
                                return ResponseEntity.badRequest()
                                                .body("Cannot restore: Not enough stock available. Only "
                                                                + product.getStock() + " left.");
                        }

                        product.setStock(product.getStock() - payment.getQuantity());
                        productRepository.save(product);
                }

                payment.setStatus("PENDING"); // or restore previous status if tracked
                paymentRepository.save(payment);

                return ResponseEntity.ok("Order item restored");
        }

        @Transactional
        @PostMapping("/orders/{orderId}/mark-received")
        public ResponseEntity<?> markOrderAsReceived(@PathVariable Long orderId, Principal principal) {
                try {
                        Optional<Order> maybeOrder = orderRepository.findById(orderId);
                        if (maybeOrder.isEmpty()) {
                                System.out.println("No order found with id: " + orderId);
                        }

                        Order order = maybeOrder.orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Order not found with id: " + orderId));

                        if (!order.getUser().getUsername().equals(principal.getName())) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body("You can only mark your own orders as received");
                        }

                        // ✅ Update order status
                        order.setStatus("COMPLETED");
                        orderRepository.save(order);

                        // ✅ Update all associated payments' status to COMPLETED
                        List<Payment> payments = paymentRepository.findByOrderId(orderId);
                        for (Payment payment : payments) {
                                payment.setStatus("COMPLETED");
                                paymentRepository.save(payment);
                        }

                        return ResponseEntity.ok("Order marked as received");
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error updating order: " + e.getMessage());
                }
        }

}