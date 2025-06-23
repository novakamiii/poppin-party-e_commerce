package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import org.springframework.stereotype.Repository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrders;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ArchivedOrdersRepository extends JpaRepository<ArchivedOrders, Long> {
    void deleteAllByUserId(Long userId);

    List<ArchivedOrders> findByUserId(Long userId);
}
