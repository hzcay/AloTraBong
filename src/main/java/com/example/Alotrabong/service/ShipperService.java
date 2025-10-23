package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Shipper;

import java.util.List;

public interface ShipperService {
    
    List<Shipper> getAllShippers();
    
    Shipper getShipperById(String shipperId);
    
    Shipper createShipper(Shipper shipper);
    
    Shipper updateShipper(String shipperId, Shipper shipper);
    
    void deleteShipper(String shipperId);
}