package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    long countByIsActive(Boolean isActive);

    Optional<Branch> findByBranchCodeIgnoreCase(String branchCode);

    Optional<Branch> findFirstByIsActiveTrueOrderByCreatedAtAsc();

    List<Branch> findByCityAndIsActiveTrue(String city);

    List<Branch> findByIsActiveTrue();
}