package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByUserId(Long userId);
    Optional<OrderItem> findByUserIdAndProductRef(Long userId, String productRef);
    void deleteByUserIdAndProductRef(Long userId, String productRef);
}
