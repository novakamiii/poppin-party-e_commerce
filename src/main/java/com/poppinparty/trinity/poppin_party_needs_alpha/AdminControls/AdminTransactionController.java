/**
 * Controller for admin transaction management, including viewing and updating order/payment statuses.
 * <p>
 * Endpoints:
 * <ul>
 *   <li><b>GET /admin/transaction-approval</b>: Displays a list of payments filtered by status for admin approval.</li>
 *   <li><b>POST /admin/transaction/{orderId}/status</b>: Updates the status of a specific order and sends notifications to the user.</li>
 * </ul>
 * 
 * Dependencies:
 * <ul>
 *   <li>{@link PaymentRepository} for accessing payment data.</li>
 *   <li>{@link OrderRepository} for accessing order data.</li>
 *   <li>{@link OrderStatusService} for updating order statuses.</li>
 *   <li>{@link NotificationService} for sending notifications to users.</li>
 * </ul>
 * 
 * Inner Classes:
 * <ul>
 *   <li><b>StatusUpdateRequest</b>: DTO for receiving new status updates in requests.</li>
 * </ul>
 * 
 * Handles validation of status updates and ensures notifications are sent for relevant status changes.
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.AdminControls;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.OrderStatusService;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.NotificationService;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;

@Controller
@RequestMapping("/admin")
public class AdminTransactionController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository; // Add this

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/transaction-approval")
    public String getOrdersPage(
            @RequestParam(required = false, defaultValue = "all") String filter,
            Model model) {

        List<Payment> payments;
        if ("all".equals(filter)) {
            payments = paymentRepository.findAll();
        } else {
            payments = paymentRepository.findByStatus(filter);
        }

        model.addAttribute("payments", payments);
        model.addAttribute("currentFilter", filter);
        return "shipping_approval";
    }

    @PostMapping("/transaction/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody StatusUpdateRequest request) {

        // Validate status
        if (!Arrays.asList(Order.PENDING, Order.TO_SHIP, Order.TO_RECEIVE,
                Order.COMPLETED, Order.CANCELLED).contains(request.getNewStatus())) {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        // Get the order first
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update status
        orderStatusService.updateOrderStatus(orderId, request.getNewStatus());

        // Send notification based on new status
        String message = "";
        switch (request.getNewStatus()) { // Use request.getNewStatus() instead of undefined newStatus
            case "TO_SHIP":
                message = "Your order #" + orderId + " is being prepared for shipping";
                break;
            case "TO_RECEIVE":
                message = "Your order #" + orderId + " has been shipped";
                break;
            case "COMPLETED":
                message = "Your order #" + orderId + " has been delivered";
                break;
            case "CANCELLED":
                message = "Your order #" + orderId + " has been cancelled";
                break;
        }

        // Only send notification if there's a message (skip for PENDING)
        if (!message.isEmpty()) {
            notificationService.createNotification(
                    order.getUser(),
                    message,
                    order.getTrackingNumber(),
                    request.getNewStatus(),
                    orderId);
        }

        return ResponseEntity.ok().build();
    }

    // Status DTO
    public static class StatusUpdateRequest {
        private String newStatus;

        // Getters and setters
        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }
    }
}
