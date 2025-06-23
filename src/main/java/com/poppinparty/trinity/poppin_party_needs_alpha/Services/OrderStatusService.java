package com.poppinparty.trinity.poppin_party_needs_alpha.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderStatusService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setStatus(newStatus);

        // Generate tracking number when shipping
        if (Order.TO_SHIP.equals(newStatus) && order.getTrackingNumber() == null) {
            order.setTrackingNumber(order.generateTrackingNumber());
        }

        orderRepository.save(order);

        // Update all related payments
        paymentRepository.updateStatusByOrderId(orderId, newStatus);
    }

}
