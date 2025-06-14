package com.poppinparty.trinity.poppin_party_needs_alpha;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Product;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    List<Product> findByCategory(String categoryName);
}
