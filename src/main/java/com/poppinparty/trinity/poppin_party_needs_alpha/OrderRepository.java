package com.poppinparty.trinity.poppin_party_needs_alpha;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(String status);

    // Optionally: custom finder
    Order findByTrackingNumber(String trackingNumber);
}

