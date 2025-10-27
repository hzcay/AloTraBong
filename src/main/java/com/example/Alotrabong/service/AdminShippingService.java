package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ShipperDTO;
import com.example.Alotrabong.dto.ShippingRateDTO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminShippingService {

    // Shipping Rate Management
    Page<ShippingRateDTO> getAllShippingRates(Pageable pageable);
    List<ShippingRateDTO> getAllShippingRates();
    ShippingRateDTO getShippingRateById(Integer rateId);
    ShippingRateDTO getShippingRateByBranchId(String branchId);
    ShippingRateDTO createShippingRate(ShippingRateDTO dto);
    ShippingRateDTO updateShippingRate(Integer rateId, ShippingRateDTO dto);
    void deleteShippingRate(Integer rateId);
    void activateShippingRate(Integer rateId);
    void deactivateShippingRate(Integer rateId);

    // Shipper Management
    Page<ShipperDTO> getAllShippers(Pageable pageable, String search);
    List<ShipperDTO> getAllShippers();
    List<ShipperDTO> getActiveShippers();
    ShipperDTO getShipperById(String shipperId);
    ShipperDTO createShipper(ShipperDTO dto);
    ShipperDTO updateShipper(String shipperId, ShipperDTO dto);
    void deleteShipper(String shipperId);
    void activateShipper(String shipperId);
    void deactivateShipper(String shipperId);

    // Statistics
    long getTotalShippingRatesCount();
    long getActiveShippingRatesCount();
    long getTotalShippersCount();
    long getActiveShippersCount();
}