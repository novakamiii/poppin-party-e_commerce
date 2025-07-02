package com.poppinparty.trinity.poppin_party_needs_alpha.AdminControls;
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
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.NotificationService;

@Controller
@RequestMapping("/admin")
public class AdminTransactionController {

    @Autowired
    private PaymentRepository paymentRepository;

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

    @PostMapping("/transaction/item/{paymentId}/status")
    public ResponseEntity<?> updateItemStatus(
            @PathVariable Long paymentId,
            @RequestBody StatusUpdateRequest request) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment item not found"));

        payment.setStatus(request.getNewStatus());
        paymentRepository.save(payment);

        //send a notification specific to this item
        String message = switch (request.getNewStatus()) {
            case "TO_SHIP" -> "An item in your order is being prepared for shipping";
            case "TO_RECEIVE" -> "An item in your order has been shipped";
            case "COMPLETED" -> "An item in your order has been delivered";
            case "CANCELLED" -> "An item in your order has been cancelled";
            default -> "";
        };

        if (!message.isEmpty()) {
            notificationService.createNotification(
                    payment.getUser(),
                    message,
                    payment.getOrder().getTrackingNumber(),
                    request.getNewStatus(),
                    payment.getOrder().getId());
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
