package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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
                    Product product = productRepository.findByItemName(orderItem.getProductRef())
                            .orElseThrow();
                    CartItemDTO dto = new CartItemDTO();
                    dto.setProductId(product.getId());
                    dto.setItemName(product.getItemName());
                    dto.setImageLoc(product.getImageLoc());
                    dto.setUnitPrice(orderItem.getUnitPrice().doubleValue());
                    dto.setQuantity(orderItem.getQuantity());
                    return dto;
                })
                .toList();
    }

    @PostMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<?> addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String productRef = product.getItemName();

        OrderItem item = orderItemRepository.findByUserIdAndProductRef(user.getId(), productRef)
                .orElseGet(() -> {
                    OrderItem newItem = new OrderItem();
                    newItem.setUserId(user.getId());
                    newItem.setProductRef(productRef);
                    newItem.setQuantity(0);
                    newItem.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        orderItemRepository.save(item);

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
            Long productId = Long.valueOf(item.get("productId").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
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
            Long productId = Long.valueOf(item.get("productId").toString());
            int quantity = Integer.parseInt(item.get("quantity").toString());

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            BigDecimal price = BigDecimal.valueOf(product.getPrice());

            Payment payment = new Payment();
            payment.setUser(user);
            payment.setOrder(order);
            payment.setProductId(productId.toString());
            payment.setItemName(product.getItemName());
            payment.setAmount(price.multiply(BigDecimal.valueOf(quantity)));
            payment.setTransactionId(trackingNumber);
            payment.setStatus("PENDING");
            payment.setShippingOption(shippingOption);
            payment.setPaymentMethodDetails(paymentMethod);
            payment.setDaysLeft(5);
            payment.setQuantity(quantity);

            paymentRepository.save(payment);

        }

        // Remove from cart if needed
        // orderItemRepository.deleteByUserId(user.getId());

        redirectAttributes.addFlashAttribute("trackingNumber", trackingNumber);
        return "redirect:/order/success";
    }

    @PostMapping("/api/orders/cancel/{id}")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        if (!payment.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("Only pending orders can be cancelled.");
        }

        // Update status and ETA
        payment.setStatus("CANCELLED");
        payment.setDaysLeft(0); // You can change this to -1 or null if needed
        paymentRepository.save(payment);

        return ResponseEntity.ok("Order cancelled.");
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
                    Product product = productRepository.findByItemName(payment.getItemName()).orElseThrow();
                    return new OrderStatusDTO(
                            payment.getId(),
                            payment.getItemName(),
                            payment.getAmount(),
                            product.getImageLoc(),
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
/*  */