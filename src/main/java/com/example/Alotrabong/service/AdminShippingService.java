package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ShippingRateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminShippingService {

    // Shipping Rate Management
    Page<ShippingRateDTO> getAllShippingRates(Pageable pageable);

    ShippingRateDTO getShippingRateById(String rateId);

    ShippingRateDTO createShippingRate(ShippingRateDTO dto);

    ShippingRateDTO updateShippingRate(String rateId, ShippingRateDTO dto);

    void deleteShippingRate(String rateId);

    void activateShippingRate(String rateId);

    void deactivateShippingRate(String rateId);

    ShippingRateDTO getShippingRateByDistrict(String district, String city);

    List<ShippingRateDTO> getAllActiveShippingRates();
}

