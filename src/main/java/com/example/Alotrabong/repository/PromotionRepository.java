package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    
    List<Promotion> findByIsActiveTrue();
    
    List<Promotion> findByBranchIdAndIsActiveTrue(String branchId);
}