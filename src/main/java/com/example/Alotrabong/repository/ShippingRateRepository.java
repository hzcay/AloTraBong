package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, Integer> {
    
    List<ShippingRate> findByBranch_BranchId(String branchId);
    
    Optional<ShippingRate> findByBranch_BranchIdAndIsActiveTrue(String branchId);
    
    @Query("SELECT sr FROM ShippingRate sr WHERE sr.branch.branchId = :branchId AND sr.isActive = true")
    Optional<ShippingRate> findActiveByBranchId(@Param("branchId") String branchId);
    
    @Query("SELECT COUNT(sr) FROM ShippingRate sr WHERE sr.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
}