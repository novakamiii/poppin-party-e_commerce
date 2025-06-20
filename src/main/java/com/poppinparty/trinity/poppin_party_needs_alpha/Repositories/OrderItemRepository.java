package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByUserId(Long userId);

    Optional<OrderItem> findByUserIdAndProductRef(Long userId, String productRef);

    void deleteByUserIdAndProductRef(Long userId, String productRef);

    @Query("SELECT o FROM OrderItem o WHERE o.userId = :userId AND o.customSize = :customSize AND o.eventType = :eventType AND o.personalizedMessage = :message AND o.tarpaulinThickness = :thickness AND o.tarpaulinFinish = :finish")
    Optional<OrderItem> findByUserIdAndCustomFields(
            @Param("userId") Long userId,
            @Param("customSize") String customSize,
            @Param("eventType") String eventType,
            @Param("message") String personalizedMessage,
            @Param("thickness") String tarpaulinThickness,
            @Param("finish") String tarpaulinFinish);

}
