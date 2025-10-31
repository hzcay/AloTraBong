package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.entity.PaymentStatus;
import com.example.Alotrabong.entity.Shipment;
import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.OrderRepository;
import com.example.Alotrabong.repository.ShipmentRepository;
import com.example.Alotrabong.repository.ShipperRepository;
import com.example.Alotrabong.service.ShipperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipperServiceImpl implements ShipperService {

    private final ShipperRepository shipperRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Shipper> getAllShippers() {
        return shipperRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Shipper getShipperById(String shipperId) {
        return shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found"));
    }

    @Override
    public Shipper createShipper(Shipper shipper) {
        log.info("Creating shipper for user: {}", shipper.getUser().getUserId());
        Shipper savedShipper = shipperRepository.save(shipper);
        log.info("Shipper created successfully: {}", savedShipper.getShipperId());
        return savedShipper;
    }

    @Override
    public Shipper updateShipper(String shipperId, Shipper shipper) {
        log.info("Updating shipper: {}", shipperId);
        Shipper existingShipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found"));

        existingShipper.setUser(shipper.getUser());
        existingShipper.setBranch(shipper.getBranch());
        existingShipper.setVehiclePlate(shipper.getVehiclePlate());
        existingShipper.setIsActive(shipper.getIsActive());

        existingShipper = shipperRepository.save(existingShipper);
        log.info("Shipper updated successfully: {}", shipperId);
        return existingShipper;
    }

    @Override
    public void deleteShipper(String shipperId) {
        log.info("Deleting shipper: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found"));

        shipper.setIsActive(false);
        shipperRepository.save(shipper);

        log.info("Shipper deactivated: {}", shipperId);
    }

    // ===== NGHIỆP VỤ GIAO HÀNG =====
    @Override
    @Transactional(readOnly = true)
    public List<Shipment> getAssignedShipments(String shipperId) {
        log.info("===> Fetching shipments for shipperId={} (status=0)", shipperId);
        List<Shipment> list = shipmentRepository.findByShipper_ShipperId(shipperId);
        log.info("===> Found {} shipments", list.size());
        for (Shipment s : list) {
            log.info("Shipment: id={}, orderId={}, status={}",
                    s.getShipmentId(),
                    s.getOrder().getOrderId(),
                    s.getStatus());
        }
        return list;
    }

    @Override
    public void confirmPickup(String shipmentId) {
        log.info("Shipper confirming pickup for shipment: {}", shipmentId);
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        // Cập nhật trạng thái shipment
        shipment.setStatus(1);
        shipment.setPickupTime(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Đồng bộ trạng thái order tương ứng
        Order order = shipment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.DELIVERING);
            orderRepository.save(order);
        }

        log.info("Shipment {} set to DELIVERING successfully", shipmentId);
    }

    @Override
    public void confirmDelivered(String shipmentId) {
        log.info("Confirming delivered for shipment: {}", shipmentId);
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (shipment.getOrder() == null) {
            log.warn("Shipment {} không có đơn hàng liên kết!", shipmentId);
            return;
        }

        shipment.setStatus(2);
        shipment.setDeliveredTime(LocalDateTime.now());
        shipmentRepository.save(shipment);

        Order order = shipment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("✅ Shipment {} -> DELIVERED OK", shipmentId);
    }

    // ========================= THỐNG KÊ DÀNH CHO SHIPPER =========================
    @Override
    @Transactional(readOnly = true)
    public long countPickedUp(String shipperId) {
        long count = shipmentRepository.countByShipper_ShipperIdAndStatus(shipperId, 1);
        log.info("Shipper {} đã nhận {} đơn giao hàng", shipperId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countDelivered(String shipperId) {
        long count = shipmentRepository.countByShipper_ShipperIdAndStatus(shipperId, 2);
        log.info("Shipper {} đã giao thành công {} đơn", shipperId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totalDeliveredAmount(String shipperId) {
        BigDecimal total = shipmentRepository.sumDeliveredAmount(shipperId);
        if (total == null)
            total = BigDecimal.ZERO;
        log.info("Tổng tiền shipper {} đã giao: {}", shipperId, total);
        return total;
    }

    @Override
    public List<Shipment> filterShipments(String shipperId, Integer status, LocalDate from, LocalDate to) {
        LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime end = (to != null) ? to.atTime(23, 59, 59) : LocalDateTime.now();

        if (status != null) {
            return shipmentRepository.findByShipper_ShipperIdAndStatusAndCreatedAtBetween(shipperId, status, start,
                    end);
        } else {
            return shipmentRepository.findByShipper_ShipperIdAndCreatedAtBetween(shipperId, start, end);
        }
    }

}
