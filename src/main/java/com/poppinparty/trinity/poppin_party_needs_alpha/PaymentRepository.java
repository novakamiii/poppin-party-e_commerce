package com.poppinparty.trinity.poppin_party_needs_alpha;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Optional: fetch all payments for a specific user
    List<Payment> findByUserId(Long userId);

    // Optional: fetch all payments for a specific order
    List<Payment> findByOrderId(Long orderId);

    // Optional: fetch by user and order together
    List<Payment> findByUserIdAndOrderId(Long userId, Long orderId);

    // Optional: fetch by tracking number (if you use it as transaction_id)
    List<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserIdAndStatus(Long userId, String status);
}
