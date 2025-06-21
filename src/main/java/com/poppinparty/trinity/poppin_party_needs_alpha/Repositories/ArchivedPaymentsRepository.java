package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import org.springframework.stereotype.Repository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedPayments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArchivedPaymentsRepository extends JpaRepository<ArchivedPayments, Long> {

    void deleteAllByUserId(Long userId);

    List<ArchivedPayments> findByUserId(Long userId);

    @Query("SELECT a FROM ArchivedPayments a LEFT JOIN FETCH a.order WHERE a.userId = :userId")
    List<ArchivedPayments> findByUserIdWithOrder(@Param("userId") Long userId);

}
