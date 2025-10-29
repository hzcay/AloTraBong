package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.Shipment;
import com.example.Alotrabong.entity.Shipper;
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
    
    List<Shipment> findByOrder(Order order);
    
    List<Shipment> findByShipper_Branch_BranchIdAndStatus(String branchId, Integer status);
    
    @Query("SELECT s FROM Shipment s WHERE s.shipper.branch.branchId = :branchId ORDER BY s.createdAt DESC")
    List<Shipment> findByBranchOrderByCreatedAtDesc(@Param("branchId") String branchId);
    
    // Find shipments by shipper
    List<Shipment> findByShipper(Shipper shipper);
    List<Shipment> findByShipperOrderByCreatedAtDesc(Shipper shipper);
}
