package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    
    List<Shipment> findByShipper_Branch_BranchId(String branchId);
    
    Optional<Shipment> findByShipmentIdAndShipper_Branch_BranchId(String shipmentId, String branchId);
    
    List<Shipment> findByShipper_Branch_BranchIdAndStatus(String branchId, Integer status);
    
    @Query("SELECT s FROM Shipment s WHERE s.shipper.branch.branchId = :branchId ORDER BY s.createdAt DESC")
    List<Shipment> findByBranchOrderByCreatedAtDesc(@Param("branchId") String branchId);
}
