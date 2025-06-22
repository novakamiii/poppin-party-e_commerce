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
import jakarta.transaction.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.CartItemDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderStatusDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

@Controller
public class OrdersController {
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

        @Transactional
        @PostMapping("/order/buy-now")
        @ResponseBody
        public ResponseEntity<?> buyNow(
                        @RequestParam Long productId,
                        @RequestParam int quantity,
                        Principal principal) {

                try {
                        User user = userRepository.findByUsername(principal.getName())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        // Clear current cart
                        orderItemRepository.deleteAllByUserId(user.getId());

                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                        if (product.getStock() < quantity) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(Map.of("error",
                                                                "Only " + product.getStock() + " items available"));
                        }

                        OrderItem newItem = new OrderItem();
                        newItem.setUserId(user.getId());
                        newItem.setProductRef(product.getItemName());
                        newItem.setQuantity(quantity);
                        newItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
                        newItem.setCustom(false);
                        orderItemRepository.save(newItem);

                        CartItemDTO dto = new CartItemDTO();
                        dto.setProductId(product.getId());
                        dto.setItemName(product.getItemName());
                        dto.setImageLoc(product.getImageLoc());
                        dto.setQuantity(quantity);
                        dto.setUnitPrice(product.getPrice());

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

                try {
                        User user = userRepository.findByUsername(principal.getName())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        ObjectMapper mapper = new ObjectMapper();
                        List<Map<String, Object>> items = mapper.readValue(itemsJson, new TypeReference<>() {
                        });

                        // Final validation
                        if (items.isEmpty()) {
                                redirectAttributes.addFlashAttribute("error", "No items selected for checkout");
                                return "redirect:/cart";
                        }

                        // Calculate subtotal
                        BigDecimal subtotal = items.stream()
                                        .map(item -> new BigDecimal(item.get("unitPrice").toString())
                                                        .multiply(BigDecimal.valueOf(Integer
                                                                        .parseInt(item.get("quantity").toString()))))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        // STOCK VALIDATION PHASE
                        for (Map<String, Object> item : items) {
                                Long productId = Long.valueOf(safeGet(item, "productId"));
                                int quantity = Integer.parseInt(safeGet(item, "quantity"));

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
                        }

                        // STOCK DEDUCTION PHASE
                        for (Map<String, Object> item : items) {
                                Long productId = Long.valueOf(safeGet(item, "productId"));
                                int quantity = Integer.parseInt(safeGet(item, "quantity"));

                                if (!isCustomProduct(productId)) {
                                        Product product = productRepository.findById(productId)
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                                        product.setStock(product.getStock() - quantity);
                                        productRepository.save(product);
                                }
                        }

                        BigDecimal shippingFee = switch (shippingOption) {
                                case "express" -> BigDecimal.valueOf(75);
                                case "overnight" -> BigDecimal.valueOf(150);
                                default -> BigDecimal.valueOf(45);
                        };
                        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.12));
                        BigDecimal total = subtotal.add(shippingFee).add(tax);

                        String trackingNumber = UUID.randomUUID().toString().substring(0, 12).toUpperCase();

                        // Save ORDER record
                        Order order = new Order();
                        order.setUserId(user.getId());
                        order.setTotalAmount(total);
                        order.setPaymentMethod(paymentMethod);
                        order.setShippingOption(shippingOption);
                        order.setShippingAddress(user.getAddress());
                        order.setStatus("PENDING");
                        order.setTrackingNumber(trackingNumber);
                        orderRepository.save(order);

                        // Save PAYMENT records (one per product)
                        for (Map<String, Object> item : items) {
                                Long productId = Long.valueOf(safeGet(item, "productId"));
                                int quantity = Integer.parseInt(safeGet(item, "quantity"));
                                BigDecimal price = new BigDecimal(safeGet(item, "unitPrice"));
                                String itemName = safeGet(item, "itemName");

                                // String imageLoc; Irrelevant

                                // 📝 Create Payment record
                                Payment payment = new Payment();
                                payment.setUser(user);
                                payment.setOrder(order);
                                payment.setItemName(itemName);
                                payment.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
                                payment.setTransactionId(trackingNumber);
                                payment.setStatus("PENDING");
                                payment.setShippingOption(shippingOption);
                                payment.setPaymentMethodDetails(paymentMethod);
                                payment.setDaysLeft(5);
                                payment.setQuantity(quantity);

                                if (productId == -1L) {
                                        // Custom product
                                        payment.setProductId(1L); // You can use a dummy product like ID 1 =
                                                                  // "Placeholder" OR
                                                                  // skip setting productId at all if not @NotNull
                                        payment.setIsCustom(true);
                                        payment.setCustomProductRef(itemName); // descriptive like "Custom Tarpaulin
                                                                               // (2x3 ft)"
                                } else {
                                        // Regular product
                                        Product product = productRepository.findById(productId)
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));
                                        payment.setProductId(product.getId());
                                        payment.setIsCustom(false);
                                        payment.setCustomProductRef(null);
                                }

                                paymentRepository.save(payment);
                        }

                        orderItemRepository.deleteByUserId(user.getId());

                        redirectAttributes.addFlashAttribute("trackingNumber", trackingNumber);
                        return "redirect:/order/success";

                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("error", "Order processing failed: " + e.getMessage());
                        return "redirect:/cart";
                }
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

                payment.setStatus("PENDING"); // or restore previous status if tracked
                paymentRepository.save(payment);

                return ResponseEntity.ok("Order item restored");
        }

}