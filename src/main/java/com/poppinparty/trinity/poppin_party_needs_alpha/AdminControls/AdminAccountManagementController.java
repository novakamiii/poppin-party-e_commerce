package com.poppinparty.trinity.poppin_party_needs_alpha.AdminControls;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrderItems;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedUsers;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedPayments;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrders;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedUserRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedOrderItemsRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedPaymentsRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedOrdersRepository;

@Controller
public class AdminAccountManagementController {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountManagementController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArchivedUserRepository archivedUserRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ArchivedOrdersRepository archivedOrdersRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ArchivedPaymentsRepository archivedPaymentsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ArchivedOrderItemsRepository archivedOrderItemsRepository;

    // ========== ACCOUNT ADMINS MANAGEMENT ==========

    @GetMapping("/admin/accountManagement")
    public String showAccounts(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin_account_management";
    }

    @GetMapping("/admin/user/restore")
    public String showArchivedUsers(Model model) {
        List<ArchivedUsers> archivedUsers = archivedUserRepository.findAll();
        model.addAttribute("archivedUsers", archivedUsers);
        return "accounts_archived";
    }

    @Transactional
    @GetMapping("/admin/user/delete/{id}")
    public String archiveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        // Archive User
        ArchivedUsers archivedUser = new ArchivedUsers();
        archivedUser.setName(user.getName());
        archivedUser.setPhone(user.getPhone());
        archivedUser.setUsername(user.getUsername());
        archivedUser.setPassword(user.getPassword());
        archivedUser.setAddress(user.getAddress());
        archivedUser.setEmail(user.getEmail());
        archivedUser.setRole(user.getRole());
        archivedUser.setImagePath(user.getImagePath());
        archivedUser.setGender(user.getGender());
        archivedUser.setBirthDate(user.getBirthDate());
        archivedUser.setLastLogin(user.getLastLogin());
        archivedUser.setOriginalUserId(user.getId());
        archivedUserRepository.saveAndFlush(archivedUser); // make sure it has an ID

        // Archive Orders
        List<Order> orders = orderRepository.findByUserId(id);
        Map<Long, ArchivedOrders> archivedOrderMap = new HashMap<>();
        for (Order order : orders) {
            ArchivedOrders archivedOrder = new ArchivedOrders();
            archivedOrder.setOriginalOrderId(order.getId());
            archivedOrder.setUserId(order.getUser().getId());
            archivedOrder.setOrderDate(order.getOrderDate());
            archivedOrder.setTotalAmount(order.getTotalAmount());
            archivedOrder.setStatus(order.getStatus());
            archivedOrder.setPaymentMethod(order.getPaymentMethod());
            archivedOrder.setShippingAddress(order.getShippingAddress());
            archivedOrder.setTrackingNumber(order.getTrackingNumber());
            archivedOrder.setShippingOption(order.getShippingOption());
            archivedOrdersRepository.save(archivedOrder);

            archivedOrderMap.put(order.getId(), archivedOrder);
        }

        // Archive Payments
        List<Payment> payments = paymentRepository.findByUserId(id);
        for (Payment payment : payments) {
            if (payment.getOrder() == null) {
                log.warn("Skipping payment {}: no order_id (required by archived_payments)", payment.getId());
                continue; // Skip this payment
            }
            ArchivedPayments archivedPayment = new ArchivedPayments();
            archivedPayment.setUserId(archivedUser.getId());
            ArchivedOrders relatedArchivedOrder = archivedOrderMap.get(payment.getOrder().getId());
            if (relatedArchivedOrder != null) {
                archivedPayment.setOrder(relatedArchivedOrder);
                archivedPayment.setOrderId(relatedArchivedOrder.getId());
            } else {
                log.warn("Skipping payment {}: related archived order not found", payment.getId());
                continue;
            }

            archivedPayment.setProductId(payment.getProductId());
            archivedPayment.setItemName(payment.getItemName());
            archivedPayment.setAmount(payment.getAmount());
            archivedPayment.setTransactionId(payment.getTransactionId());
            archivedPayment.setStatus(payment.getStatus());
            archivedPayment.setPaymentMethodDetails(payment.getPaymentMethodDetails());
            archivedPayment.setShippingOption(payment.getShippingOption());
            archivedPayment.setDaysLeft(payment.getDaysLeft());
            archivedPayment.setQuantity(payment.getQuantity());
            archivedPayment.setCustomProductRef(payment.getCustomProductRef());
            archivedPayment.setIsCustom(payment.getIsCustom());

            archivedPaymentsRepository.save(archivedPayment);
        }

        // Archive Order Items
        List<OrderItem> orderItems = orderItemRepository.findByUserId(id);
        for (OrderItem item : orderItems) {
            ArchivedOrderItems archivedItem = new ArchivedOrderItems();
            archivedItem.setUserId(user.getId()); // original userId is correct here
            archivedItem.setProductRef(item.getProductRef());
            archivedItem.setQuantity(item.getQuantity());
            archivedItem.setUnitPrice(item.getUnitPrice());
            archivedItem.setCustom(item.isCustom());
            archivedItem.setCustomSize(item.getCustomSize());
            archivedItem.setEventType(item.getEventType());
            archivedItem.setPersonalizedMessage(item.getPersonalizedMessage());
            archivedItem.setTarpaulinThickness(item.getTarpaulinThickness());
            archivedItem.setTarpaulinFinish(item.getTarpaulinFinish());
            archivedOrderItemsRepository.save(archivedItem);
        }

        // Delete in the correct order
        paymentRepository.deleteAll(payments);
        orderItemRepository.deleteAll(orderItems);
        orderRepository.deleteAll(orders);
        userRepository.deleteById(id);

        return "redirect:/admin/accountManagement";
    }

    @Transactional
    @PostMapping("/admin/user/restore/{id}")
    public String restoreUser(@PathVariable Long id) {
        // Find archived user
        ArchivedUsers archivedUser = archivedUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archived user not found: " + id));

        log.info("Starting restoration of user. Archived ID: {}, Original ID: {}", id,
                archivedUser.getOriginalUserId());

        // Restore User
        User restoredUser = new User();
        restoredUser.setName(archivedUser.getName());
        restoredUser.setPhone(archivedUser.getPhone());
        restoredUser.setUsername(archivedUser.getUsername());
        restoredUser.setPassword(archivedUser.getPassword());
        restoredUser.setAddress(archivedUser.getAddress());
        restoredUser.setEmail(archivedUser.getEmail());
        restoredUser.setRole(archivedUser.getRole());
        restoredUser.setImagePath(archivedUser.getImagePath());
        restoredUser.setGender(archivedUser.getGender());
        restoredUser.setBirthDate(archivedUser.getBirthDate());
        restoredUser.setLastLogin(archivedUser.getLastLogin());

        User savedUser = userRepository.save(restoredUser);
        log.info("Restored user with new ID: {}", savedUser.getId());

        Long originalUserId = archivedUser.getOriginalUserId();

        // Restore Orders
        List<ArchivedOrders> archivedOrders = archivedOrdersRepository.findByUserId(originalUserId);
        Map<Long, Order> orderIdMapping = new HashMap<>();
        log.info("Found {} orders to restore", archivedOrders.size());

        for (ArchivedOrders archivedOrder : archivedOrders) {
            Order order = new Order();
            order.setUser(savedUser);
            order.setOrderDate(archivedOrder.getOrderDate());
            order.setTotalAmount(archivedOrder.getTotalAmount());
            order.setStatus(archivedOrder.getStatus() != null ? archivedOrder.getStatus() : "PENDING");
            order.setPaymentMethod(archivedOrder.getPaymentMethod());
            order.setShippingAddress(archivedOrder.getShippingAddress());
            order.setTrackingNumber(archivedOrder.getTrackingNumber());
            order.setShippingOption(archivedOrder.getShippingOption());

            Order savedOrder = orderRepository.save(order);
            orderIdMapping.put(archivedOrder.getId(), savedOrder);
            log.debug("Restored order ID {} (original {})", savedOrder.getId(), archivedOrder.getId());
        }

        // Restore Order Items
        List<ArchivedOrderItems> archivedItems = archivedOrderItemsRepository.findByUserId(originalUserId);
        log.info("Found {} order items to restore", archivedItems.size());

        for (ArchivedOrderItems archivedItem : archivedItems) {
            OrderItem item = new OrderItem();
            item.setUserId(savedUser.getId());
            item.setProductRef(archivedItem.getProductRef());
            item.setQuantity(archivedItem.getQuantity());
            item.setUnitPrice(archivedItem.getUnitPrice());
            item.setCustom(archivedItem.isCustom());
            item.setCustomSize(archivedItem.getCustomSize());
            item.setEventType(archivedItem.getEventType());
            item.setPersonalizedMessage(archivedItem.getPersonalizedMessage());
            item.setTarpaulinThickness(archivedItem.getTarpaulinThickness());
            item.setTarpaulinFinish(archivedItem.getTarpaulinFinish());

            if (archivedItem.getOrder() != null) {
                Order newOrder = orderIdMapping.get(archivedItem.getOrder().getId());
                if (newOrder != null) {
                    item.setOrder(newOrder);
                } else {
                    log.warn("Could not find restored order for item {}", archivedItem.getId());
                }
            }

            orderItemRepository.save(item);
        }

        // Restore Payments
        List<ArchivedPayments> archivedPayments = archivedPaymentsRepository.findByUserId(id); // Using archived user ID
        log.info("Found {} payments to restore for archived user ID {}", archivedPayments.size(), id);

        for (ArchivedPayments archivedPayment : archivedPayments) {
            Payment payment = new Payment();
            payment.setUser(savedUser);

            /// Handle order reference
            if (archivedPayment.getOrderId() != null) { // Check order_id directly
                Order newOrder = orderIdMapping.get(archivedPayment.getOrderId());
                if (newOrder != null) {
                    payment.setOrder(newOrder);
                }
            }

            // Copy all payment
            payment.setStatus(archivedPayment.getStatus() != null ? archivedPayment.getStatus() : "PENDING");
            payment.setProductId(archivedPayment.getProductId());
            payment.setItemName(archivedPayment.getItemName());
            payment.setAmount(archivedPayment.getAmount() != null ? archivedPayment.getAmount() : BigDecimal.ZERO);
            payment.setTransactionId(archivedPayment.getTransactionId());
            payment.setPaymentMethodDetails(archivedPayment.getPaymentMethodDetails());
            payment.setShippingOption(archivedPayment.getShippingOption());
            payment.setDaysLeft(archivedPayment.getDaysLeft());
            payment.setQuantity(archivedPayment.getQuantity() != null ? archivedPayment.getQuantity() : 0);
            payment.setCustomProductRef(archivedPayment.getCustomProductRef());
            payment.setIsCustom(archivedPayment.getIsCustom() != null ? archivedPayment.getIsCustom() : false);

            Payment savedPayment = paymentRepository.save(payment);
            log.debug("Restored payment ID {}", savedPayment.getId());
        }
        // Cleanup
        try {
            archivedPaymentsRepository.deleteAllByUserId(id);
            archivedOrderItemsRepository.deleteAllByUserId(originalUserId);
            archivedOrdersRepository.deleteAllByUserId(originalUserId);
            archivedUserRepository.deleteById(id);

            log.info("Cleanup completed for archived user ID {}", id);
        } catch (Exception e) {
            log.error("Error during cleanup: ", e);
            throw new RuntimeException("Cleanup failed", e);
        }

        return "redirect:/admin/accountManagement?restoreSuccess=true";
    }
}
