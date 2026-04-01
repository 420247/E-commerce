package com.shop.ecommerce.repository;

import com.shop.ecommerce.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Product entity. Contains custom queries for filtering products. */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Filter products by category, price range and minimum rating. All parameters are optional — if
   * null, that filter is ignored.
   */
  @Query(
      "SELECT p FROM Product p WHERE "
          + "(:category IS NULL OR p.category = :category) AND "
          + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
          + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
          + "(:minRating IS NULL OR p.rating >= :minRating)")
  List<Product> findWithFilters(
      @Param("category") String category,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("minRating") BigDecimal minRating);
}
