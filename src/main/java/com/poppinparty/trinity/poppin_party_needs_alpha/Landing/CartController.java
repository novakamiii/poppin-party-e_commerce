/**
 * Controller for managing the shopping cart functionality.
 * Handles adding, updating, removing, and validating cart items for users.
 *
 * <p>Endpoints provided:
 * <ul>
 *   <li><b>GET /cart</b>: Returns the cart view page.</li>
 *   <li><b>GET /api/cart/count</b>: Returns the total number of items in the user's cart.</li>
 *   <li><b>GET /api/cart</b>: Returns a list of cart items for the logged-in user.</li>
 *   <li><b>POST /api/cart/add</b>: Adds a product to the user's cart, with stock validation.</li>
 *   <li><b>POST /api/cart/validate-checkout</b>: Validates stock for all items before checkout.</li>
 *   <li><b>POST /api/cart/update</b>: Updates the quantity of a specific product in the cart.</li>
 *   <li><b>POST /api/cart/remove</b>: Removes a product or custom tarpaulin from the cart.</li>
 *   <li><b>POST /api/cart/custom-tarpaulin</b>: Adds a custom tarpaulin item to the cart.</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>{@link OrderItemRepository} for cart item persistence.</li>
 *   <li>{@link ProductRepository} for product data and stock validation.</li>
 *   <li>{@link UserRepository} for user data retrieval.</li>
 * </ul>
 *
 * <p>Handles both regular products and custom tarpaulin items with custom fields.
 * All endpoints require the user to be authenticated (except for cart count, which returns 0 for guests).
 *
 * <p>Transactional annotation is used where multiple database operations may occur.
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.CartItemDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

@Controller
public class CartController {

        @Autowired
        private OrderItemRepository orderItemRepository;
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private UserRepository userRepository;

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
                                                dto.setId(orderItem.getId());
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
                        return ResponseEntity
                                                        .status(HttpStatus.BAD_REQUEST)
                                                        .contentType(MediaType.TEXT_PLAIN)
                                                        .body("This product is out of stock!");

                }

                if (quantity > product.getStock()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .body("Only " + product.getStock() + " items available!");
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
                                return ResponseEntity
                                                .status(HttpStatus.BAD_REQUEST)
                                                .contentType(MediaType.TEXT_PLAIN)
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

                return ResponseEntity.ok("Item added to cart!");

        }

        @PostMapping("/api/cart/validate-checkout")
        @ResponseBody
        public ResponseEntity<?> validateCheckout(
                        @RequestBody Map<String, Object> payload,
                        Principal principal) {

                try {
                        User user = userRepository.findByUsername(principal.getName())
                                        .orElseThrow(() -> new RuntimeException("User not found"));

                        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

                        if (items == null || items.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body(Map.of("error", "No items selected for checkout"));
                        }

                        // Validate stock for each item
                        for (Map<String, Object> item : items) {
                                Long productId = Long.valueOf(item.get("productId").toString());
                                int quantity = Integer.parseInt(item.get("quantity").toString());

                                if (productId != -1) { // Skip custom products
                                        Product product = productRepository.findById(productId)
                                                        .orElseThrow(() -> new RuntimeException("Product not found"));

                                        if (product.getStock() < quantity) {
                                                return ResponseEntity.badRequest()
                                                                .body(Map.of(
                                                                                "error",
                                                                                "Insufficient stock for "
                                                                                                + product.getItemName(),
                                                                                "product", product.getItemName(),
                                                                                "available", product.getStock()));
                                        }
                                }
                        }

                        return ResponseEntity.ok(Map.of("success", true));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("error", "Validation error: " + e.getMessage()));
                }
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
        public ResponseEntity<?> removeItemById(@RequestParam Long id, Principal principal) {
                User user = userRepository.findByUsername(principal.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Optional<OrderItem> itemOpt = orderItemRepository.findById(id);

                if (itemOpt.isEmpty() || !itemOpt.get().getUserId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to remove this item.");
                }

                orderItemRepository.deleteById(id);
                return ResponseEntity.ok("Item removed");
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
}