package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

/**
 * Controller for managing shopping cart and order-related operations.
 * <p>
 * Handles endpoints for viewing and updating the cart, placing orders,
 * adding custom tarpaulin products, and retrieving order statuses.
 * </p>
 *
 * <h2>Endpoints:</h2>
 * <ul>
 * <li><b>GET /cart</b>: Display the cart page.</li>
 * <li><b>GET /order/success</b>: Show order success page.</li>
 * <li><b>GET /order/status</b>: Show order status page.</li>
 * <li><b>GET /order/checkout</b>: Display checkout page with user's
 * address.</li>
 * <li><b>GET /api/cart</b>: Retrieve current user's cart items as JSON.</li>
 * <li><b>POST /api/cart/update</b>: Update quantity of a product in the
 * cart.</li>
 * <li><b>POST /api/cart/remove</b>: Remove a product from the cart.</li>
 * <li><b>POST /order/place</b>: Place an order with selected items, shipping,
 * and payment method.</li>
 * <li><b>POST /api/cart/custom-tarpaulin</b>: Add a custom tarpaulin product to
 * the cart.</li>
 * <li><b>GET /api/orders</b>: Retrieve orders/payments by status for the
 * current user.</li>
 * </ul>
 *
 * <h2>Dependencies:</h2>
 * <ul>
 * <li>UserRepository - for user data access</li>
 * <li>ProductRepository - for product data access</li>
 * <li>OrderItemRepository - for cart/order item management</li>
 * <li>OrderRepository - for order records</li>
 * <li>PaymentRepository - for payment records</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 * <li>Supports both regular and custom products (e.g., custom tarpaulins).</li>
 * <li>Handles cart item CRUD operations via REST endpoints.</li>
 * <li>Processes order placement, including calculation of subtotal, shipping,
 * tax, and total.</li>
 * <li>Creates order and payment records upon order placement.</li>
 * <li>Provides order status tracking and retrieval.</li>
 * </ul>
 *
 * <h2>Security:</h2>
 * <ul>
 * <li>All endpoints require authentication (Principal is used to identify the
 * user).</li>
 * </ul>
 *
 * @author [Your Name]
 */
@Controller
public class CartController {

        @Autowired
        private com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository userRepository;
        @Autowired
        private com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository productRepository;
        @Autowired
        private com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository orderItemRepository;
        @Autowired
        private com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository orderRepository;
        @Autowired
        private com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository paymentRepository;

        // ========== ORDER MANAGEMENT ==========
        @GetMapping("/cart")
        public String viewCart() {
                return "cart"; // cart.html
        }

        @GetMapping("/order/success")
        public String orderSuccess() {
                return "ordersuccess";
        }

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

        @GetMapping("/api/cart")
        @ResponseBody
        public List<CartItemDTO> getCartItems(Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return orderItemRepository.findByUserId(user.getId())
                                .stream()
                                .map(orderItem -> {
                                        CartItemDTO dto = new CartItemDTO();
                                        dto.setQuantity(orderItem.getQuantity());
                                        dto.setUnitPrice(orderItem.getUnitPrice().doubleValue());

                                        if (Boolean.TRUE.equals(orderItem.isCustom())) {
                                                dto.setCustom(true);
                                                dto.setItemName("Custom Tarpaulin (" + orderItem.getCustomSize() + ")");
                                                dto.setImageLoc("https://placehold.co/150x100/b944fd/ffffff?font=poppins&text=Tarpulin");

                                                dto.setCustomSize(orderItem.getCustomSize());
                                                dto.setEventType(orderItem.getEventType());
                                                dto.setPersonalizedMessage(orderItem.getPersonalizedMessage());
                                                dto.setTarpaulinThickness(orderItem.getTarpaulinThickness());
                                                dto.setTarpaulinFinish(orderItem.getTarpaulinFinish());
                                                dto.setProductId(-1L);
                                        } else {
                                                Product product = productRepository
                                                                .findByItemName(orderItem.getProductRef())
                                                                .orElseThrow(() -> new RuntimeException(
                                                                                "Product not found: " + orderItem
                                                                                                .getProductRef()));

                                                dto.setCustom(false);
                                                dto.setProductId(product.getId());
                                                dto.setItemName(product.getItemName());
                                                dto.setImageLoc(product.getImageLoc());
                                        }

                                        return dto;
                                })
                                .toList();
        }

        @PostMapping("/api/cart/add")
        @ResponseBody
        public ResponseEntity<?> addItemToCart(@RequestParam Long productId,
                        @RequestParam(required = false, defaultValue = "1") int quantity,
                        Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                String productRef = product.getItemName();

                Optional<OrderItem> existing = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef);
                if (existing.isPresent()) {
                        OrderItem item = existing.get();
                        item.setQuantity(item.getQuantity() + quantity); // ✅ add quantity
                        orderItemRepository.save(item);
                } else {
                        OrderItem newItem = new OrderItem();
                        newItem.setUserId(user.getId());
                        newItem.setProductRef(productRef);
                        newItem.setQuantity(quantity); // ✅ use incoming quantity
                        newItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
                        newItem.setCustom(false);
                        orderItemRepository.save(newItem);
                }

                return ResponseEntity.ok("Item added to cart");
        }

        @PostMapping("/api/cart/update")
        @ResponseBody
        public ResponseEntity<?> updateCartQuantity(
                        @RequestParam Long productId,
                        @RequestParam int quantity,
                        Principal principal) {

                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                String productRef = product.getItemName();

                OrderItem item = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef)
                                .orElseThrow(() -> new RuntimeException("Cart item not found"));

                item.setQuantity(quantity);
                orderItemRepository.save(item);

                return ResponseEntity.ok("Quantity updated");
        }

        @PostMapping("/api/cart/remove")
        @ResponseBody
        public ResponseEntity<?> removeItemFromCart(
                        @RequestParam(required = false) Long productId,
                        @RequestParam(required = false) String customSize,
                        @RequestParam(required = false) String eventType,
                        @RequestParam(required = false) String message,
                        @RequestParam(required = false) String thickness,
                        @RequestParam(required = false) String finish,
                        Principal principal) {

                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (productId != null && productId != -1L) {
                        // Regular product
                        Product product = productRepository.findById(productId)
                                        .orElseThrow(() -> new RuntimeException("Product not found"));
                        String productRef = product.getItemName();

                        orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef)
                                        .ifPresent(orderItemRepository::delete);

                } else {
                        // Custom tarpaulin
                        orderItemRepository.findByUserIdAndCustomFields(
                                        user.getId(), customSize, eventType, message, thickness, finish)
                                        .ifPresent(orderItemRepository::delete);
                }

                return ResponseEntity.ok("Item removed");
        }

        @PostMapping("/order/place")
        public String placeOrder(
                        @RequestParam String shippingOption,
                        @RequestParam String paymentMethod,
                        @RequestParam String itemsJson,
                        Principal principal,
                        RedirectAttributes redirectAttributes) throws JsonProcessingException {

                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> items = mapper.readValue(itemsJson, new TypeReference<>() {
                });
                BigDecimal subtotal = BigDecimal.ZERO;

                for (Map<String, Object> item : items) {
                        Long productId = Long.valueOf(safeGet(item, "productId"));
                        int quantity = Integer.parseInt(safeGet(item, "quantity"));
                        BigDecimal price = new BigDecimal(safeGet(item, "unitPrice"));

                        BigDecimal itemTotal;

                        if (isCustomProduct(productId)) {
                                itemTotal = price.multiply(BigDecimal.valueOf(quantity));
                        } else {
                                Product product = productRepository.findById(productId)
                                                .orElseThrow(() -> new RuntimeException("Product not found"));
                                itemTotal = BigDecimal.valueOf(product.getPrice())
                                                .multiply(BigDecimal.valueOf(quantity));
                        }

                        subtotal = subtotal.add(itemTotal);
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

                        String imageLoc;

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
                                payment.setProductId(1L); // You can use a dummy product like ID 1 = "Placeholder" OR
                                                          // skip setting productId at all if not @NotNull
                                payment.setIsCustom(true);
                                payment.setCustomProductRef(itemName); // descriptive like "Custom Tarpaulin (2x3 ft)"
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

                // Remove from cart if needed
                // orderItemRepository.deleteByUserId(user.getId());

                redirectAttributes.addFlashAttribute("trackingNumber", trackingNumber);
                return "redirect:/order/success";
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

        @PostMapping("/api/cart/custom-tarpaulin")
        @ResponseBody
        public ResponseEntity<?> addCustomTarpaulinToCart(@RequestBody Map<String, Object> payload,
                        Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String size = payload.get("size").toString();
                String itemName = "Custom Tarpaulin (" + size + ")";
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(payload.get("price").toString()));

                OrderItem customItem = new OrderItem();
                customItem.setUserId(user.getId());
                customItem.setProductRef(itemName);
                customItem.setQuantity(1);
                customItem.setUnitPrice(price);
                customItem.setCustom(true);

                // ✅ Set all the custom fields correctly
                customItem.setCustomSize(size);
                customItem.setEventType(payload.get("eventType").toString());
                customItem.setPersonalizedMessage(payload.get("message").toString());
                customItem.setTarpaulinThickness(payload.get("thickness").toString());
                customItem.setTarpaulinFinish(payload.get("finish").toString());

                orderItemRepository.save(customItem);

                // Return updated cart (optional; you can also return just a message)
                return ResponseEntity.ok("Custom tarpaulin added to cart");
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

        @PostMapping("/api/orders/cancel/{paymentId}")
        @ResponseBody
        public ResponseEntity<?> cancelOrder(@PathVariable Long paymentId, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                if (!payment.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
                }

                if (payment.getStatus().equals("TO SHIP") || payment.getStatus().equals("TO RECEIVE")
                                || payment.getStatus().equals("COMPLETED")) {
                        return ResponseEntity.badRequest().body("Cannot cancel shipped/completed items.");
                }

                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);

                return ResponseEntity.ok("Order item cancelled");
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