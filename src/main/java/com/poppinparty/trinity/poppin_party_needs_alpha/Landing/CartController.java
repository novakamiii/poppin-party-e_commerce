package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
                                                dto.setImageLoc("/img/custom-tarp-placeholder.png");

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
        public ResponseEntity<?> removeItemFromCart(@RequestParam Long productId, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                String productRef = product.getItemName();

                orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef)
                                .ifPresent(orderItemRepository::delete);

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

                return paymentRepository.findByUserIdAndStatus(user.getId(), status).stream()
                                .map(payment -> {
                                        Optional<Product> productOpt = productRepository
                                                        .findByItemName(payment.getItemName());
                                        String imageLoc = productOpt.map(Product::getImageLoc)
                                                        .orElse("/img/custom-default.png");

                                        return new OrderStatusDTO(
                                                        payment.getId(),
                                                        payment.getItemName(),
                                                        payment.getAmount(),
                                                        imageLoc,
                                                        payment.getDaysLeft(),
                                                        payment.getShippingOption(),
                                                        payment.getStatus(),
                                                        payment.getTransactionId(),
                                                        payment.getOrder().getId(),
                                                        payment.getQuantity());
                                })
                                .toList();
        }

}