package com.example.softdevoluciones.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.softdevoluciones.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  List<Product> findByActiveTrue();

  List<Product> findByCategoryId(UUID categoryId);

  @Query("""
      SELECT p FROM Product p
      WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
        AND (:onlyActive = false OR p.active = true)
      """)
  Page<Product> findByFilters(
      @Param("categoryId") UUID categoryId,
      @Param("name") String name,
      @Param("onlyActive") boolean onlyActive,
      Pageable pageable);
}
