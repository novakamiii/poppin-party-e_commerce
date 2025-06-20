package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedUsers;


@Repository
public interface ArchivedUserRepository extends JpaRepository<ArchivedUsers, Long> {

}

