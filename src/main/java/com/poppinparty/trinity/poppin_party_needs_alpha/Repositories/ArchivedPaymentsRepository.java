package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import org.springframework.stereotype.Repository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedPayments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ArchivedPaymentsRepository extends JpaRepository<ArchivedPayments, Long> {

    void deleteAllByUserId(Long userId);

    List<ArchivedPayments> findByUserId(Long userId);

}
