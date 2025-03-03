package com.tistory.kmmoon.frankit.domain.repository;

import com.tistory.kmmoon.frankit.domain.entity.Product;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * N+1일 발생하지 않도록 FetchJoin 사용
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.options o LEFT JOIN FETCH o.optionValues WHERE p.id = :id")
    Optional<Product> findByIdWithOptionsAndValues(@Param("id") Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.options WHERE p.id = :id")
    Optional<Product> findByIdWithOptions(@Param("id") Long id);

    /**
     * 생성자(이메일)로 상품 조회
     */
    Page<Product> findByCreatedBy(String createdBy, Pageable pageable);
    
    /**
     * 최종 수정자(이메일)로 상품 조회
     */
    Page<Product> findByLastModifiedBy(String lastModifiedBy, Pageable pageable);
    
    /**
     * 특정 시간 이후 생성된 상품 조회
     */
    Page<Product> findByCreatedAtAfter(LocalDateTime startDate, Pageable pageable);
    
    /**
     * 특정 시간 이후 수정된 상품 조회
     */
    Page<Product> findByUpdatedAtAfter(LocalDateTime startDate, Pageable pageable);
}