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

@Controller
@RequestMapping("/admin")
public class AdminTransactionController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderStatusService orderStatusService;

    // View all transactions with filter
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

        // Validate against your allowed statuses
        if (!Arrays.asList(Order.PENDING, Order.TO_SHIP, Order.TO_RECEIVE,
                Order.COMPLETED, Order.CANCELLED).contains(request.getNewStatus())) {
            return ResponseEntity.badRequest().body("Invalid status");
        }

        orderStatusService.updateOrderStatus(orderId, request.getNewStatus());
        return ResponseEntity.ok().build();
    }

    // Status DTO
    public static class StatusUpdateRequest {
        private String newStatus;

        public String getNewStatus() {
            return newStatus;
        }

        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }

    }
}
