package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrderItems;

@Repository
public interface ArchivedOrderItemsRepository extends JpaRepository<ArchivedOrderItems, Long> {

    void deleteAllByUserId(Long userId);

    List<ArchivedOrderItems> findByUserId(Long userId);
}
