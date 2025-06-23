package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Notification;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}
