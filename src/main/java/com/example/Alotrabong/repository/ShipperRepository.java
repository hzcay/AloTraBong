package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, String> {
    
    @EntityGraph(attributePaths = {"user", "branch"})
    List<Shipper> findByIsActiveTrue();
    
    @EntityGraph(attributePaths = {"user", "branch"})
    List<Shipper> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Shipper s WHERE s.user.fullName LIKE %:search% OR s.user.phone LIKE %:search% OR s.user.email LIKE %:search%")
    List<Shipper> findByUserDetailsContaining(@Param("search") String search);
    
    @Query("SELECT COUNT(s) FROM Shipper s WHERE s.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(s) FROM Shipper s WHERE s.isActive = true")
    long countActiveShippers();

    @EntityGraph(attributePaths = {"user", "branch"})
    List<Shipper> findByBranch_BranchId(String branchId);
    
    @EntityGraph(attributePaths = {"user", "branch"})
    Shipper findByShipperIdAndBranch_BranchId(String shipperId, String branchId);
    
    Optional<Shipper> findByUser(User user);

    Optional<Shipper> findByUser_UserId(String userId);

    Optional<Shipper> findByUser_Email(String email);
}