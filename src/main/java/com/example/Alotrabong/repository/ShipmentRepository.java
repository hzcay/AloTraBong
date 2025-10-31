package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.Shipment;
import com.example.Alotrabong.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Query("SELECT s FROM Shipment s " +
            "WHERE s.shipper.shipperId = :shipperId AND s.status = 0")
    List<Shipment> findAssignedShipments(@Param("shipperId") String shipperId);

    List<Shipment> findByShipper_ShipperId(String shipperId);

    // 1️⃣ Đếm số đơn theo trạng thái (0: chờ nhận, 1: đang giao, 2: đã giao)
    long countByShipper_ShipperIdAndStatus(String shipperId, Integer status);

    // 2️⃣ Tổng tiền tất cả đơn đã giao thành công
    @Query("SELECT SUM(o.totalAmount) " +
            "FROM Shipment s JOIN s.order o " +
            "WHERE s.shipper.shipperId = :shipperId AND s.status = 2")
    BigDecimal sumDeliveredAmount(@Param("shipperId") String shipperId);

    List<Shipment> findByShipper_ShipperIdAndCreatedAtBetween(
            String shipperId, LocalDateTime start, LocalDateTime end);

    List<Shipment> findByShipper_ShipperIdAndStatusAndCreatedAtBetween(
            String shipperId, Integer status, LocalDateTime start, LocalDateTime end);

}
