package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, String> {
    
    List<Shipper> findByIsActiveTrue();
    
    List<Shipper> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Shipper s WHERE s.user.fullName LIKE %:search% OR s.user.phone LIKE %:search% OR s.user.email LIKE %:search%")
    List<Shipper> findByUserDetailsContaining(@Param("search") String search);
    
    @Query("SELECT COUNT(s) FROM Shipper s WHERE s.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT COUNT(s) FROM Shipper s WHERE s.isActive = true")
    long countActiveShippers();
}