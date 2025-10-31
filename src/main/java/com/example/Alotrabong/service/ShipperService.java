package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Shipment;
import com.example.Alotrabong.entity.Shipper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ShipperService {
    
    List<Shipper> getAllShippers();
    
    Shipper getShipperById(String shipperId);
    
    Shipper createShipper(Shipper shipper);
    
    Shipper updateShipper(String shipperId, Shipper shipper);
    
    void deleteShipper(String shipperId);

    List<Shipment> getAssignedShipments(String shipperId);

    void confirmPickup(String shipmentId);

    void confirmDelivered(String shipmentId);

    long countPickedUp(String shipperId);

    long countDelivered(String shipperId);

    BigDecimal totalDeliveredAmount(String shipperId);
    
    List<Shipment> filterShipments(String shipperId, Integer status, LocalDate fromDate, LocalDate toDate);
}