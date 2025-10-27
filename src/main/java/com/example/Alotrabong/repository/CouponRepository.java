package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    
    List<Coupon> findByIsActiveTrue();
    
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    
    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);
}