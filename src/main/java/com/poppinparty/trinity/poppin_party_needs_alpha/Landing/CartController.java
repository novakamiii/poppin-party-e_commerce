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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

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

        @GetMapping("/api/cart/count")
        @ResponseBody
        public int getCartItemCount(Principal principal) {
                if (principal == null) {
                        return 0; // For non-logged-in users
                }

                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return orderItemRepository.sumQuantityByUserId(user.getId());
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

        @GetMapping("/order/success")
        public String orderSuccess() {
                return "ordersuccess";
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

        @Transactional
        @PostMapping("/api/cart/add")
        @ResponseBody
        public ResponseEntity<?> addItemToCart(
                        @RequestParam Long productId,
                        @RequestParam(required = false, defaultValue = "1") int quantity,
                        Principal principal) {

                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                // Stock validation
                if (product.getStock() <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("This product is out of stock");
                }

                if (quantity > product.getStock()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body("Only " + product.getStock() + " items available");
                }

                String productRef = product.getItemName();

                // Changed to handle multiple results
                List<OrderItem> existingItems = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef);
                if (!existingItems.isEmpty()) {
                        // Sum all existing quantities
                        int totalQuantity = existingItems.stream()
                                        .mapToInt(OrderItem::getQuantity)
                                        .sum() + quantity;

                        if (totalQuantity > product.getStock()) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Cannot add more than available stock (Max: " + product.getStock()
                                                                + ")");
                        }

                        // Update first item and delete duplicates
                        OrderItem itemToUpdate = existingItems.get(0);
                        itemToUpdate.setQuantity(totalQuantity);
                        orderItemRepository.save(itemToUpdate);

                        // Delete duplicates if any
                        if (existingItems.size() > 1) {
                                orderItemRepository.deleteAll(existingItems.subList(1, existingItems.size()));
                        }
                } else {
                        OrderItem newItem = new OrderItem();
                        newItem.setUserId(user.getId());
                        newItem.setProductRef(productRef);
                        newItem.setQuantity(quantity);
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

                List<OrderItem> items = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef);
                if (items.isEmpty()) {
                        throw new RuntimeException("Cart item not found");
                }
                OrderItem item = items.get(0);

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

                        List<OrderItem> items = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef);
                        if (!items.isEmpty()) {
                                orderItemRepository.deleteAll(items);
                        }

                } else {
                        // Custom tarpaulin
                        orderItemRepository.findByUserIdAndCustomFields(
                                        user.getId(), customSize, eventType, message, thickness, finish)
                                        .ifPresent(orderItemRepository::delete);
                }

                return ResponseEntity.ok("Item removed");
        }

        @Transactional
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
                                                                        ". Only " + product.getStock() + " left.");
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

                orderItemRepository.deleteByUserId(user.getId());

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