package com.poppinparty.trinity.poppin_party_needs_alpha.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findByCategory(String categoryName);
    Optional<Product> findByItemName(String itemName);
}
