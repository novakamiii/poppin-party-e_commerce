package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Optional: fetch all payments for a specific user
    List<Payment> findByUserId(Long userId); // ✅ Add this line

    // Optional: fetch all payments for a specific order
    List<Payment> findByOrderId(Long orderId);

    // Optional: fetch by user and order together
    List<Payment> findByUserIdAndOrderId(Long userId, Long orderId);

    // Optional: fetch by tracking number (if you use it as transaction_id)
    List<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserIdAndStatus(Long userId, String status);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order JOIN FETCH p.user WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") String status);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus WHERE p.order.id = :orderId")
    void updateStatusByOrderId(@Param("orderId") Long orderId, @Param("newStatus") String newStatus);

}
