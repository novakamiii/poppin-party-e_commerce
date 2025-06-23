package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    @Override
    Optional<Category> findById(Long id);
    Optional<Category> findByDescription(String description);
    Category findByNameAndDescriptionAndId(String name, String description, Long id);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name")
    boolean existsByName(String name);

}
