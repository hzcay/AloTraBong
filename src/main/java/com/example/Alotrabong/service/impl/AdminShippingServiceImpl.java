package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ShippingRateDTO;
import com.example.Alotrabong.entity.ShippingRate;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.ShippingRateRepository;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.service.AdminShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminShippingServiceImpl implements AdminShippingService {

    private final ShippingRateRepository shippingRateRepository;
    private final BranchRepository branchRepository;

    @Override
    public Page<ShippingRateDTO> getAllShippingRates(Pageable pageable) {
        log.info("Fetching all shipping rates");
        Page<ShippingRate> rates = shippingRateRepository.findAll(pageable);
        return rates.map(this::convertToDTO);
    }

    @Override
    public ShippingRateDTO getShippingRateById(String rateId) {
        log.info("Fetching shipping rate by id: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(Integer.parseInt(rateId))
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        return convertToDTO(rate);
    }

    @Override
    public ShippingRateDTO createShippingRate(ShippingRateDTO dto) {
        log.info("Creating new shipping rate for district: {}", dto.getDistrict());
        Branch branch = branchRepository.findById(dto.getDistrict())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        ShippingRate rate = ShippingRate.builder()
                .branch(branch)
                .baseFee(dto.getBaseRate())
                .perKmFee(dto.getPerKmRate())
                .freeShipThreshold(dto.getMaxDistance())
                .build();

        rate = shippingRateRepository.save(rate);
        return convertToDTO(rate);
    }

    @Override
    public ShippingRateDTO updateShippingRate(String rateId, ShippingRateDTO dto) {
        log.info("Updating shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(Integer.parseInt(rateId))
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));

        rate.setBaseFee(dto.getBaseRate());
        rate.setPerKmFee(dto.getPerKmRate());
        rate.setFreeShipThreshold(dto.getMaxDistance());

        rate = shippingRateRepository.save(rate);
        return convertToDTO(rate);
    }

    @Override
    public void deleteShippingRate(String rateId) {
        log.info("Deleting shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(Integer.parseInt(rateId))
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        shippingRateRepository.delete(rate);
    }

    @Override
    public void activateShippingRate(String rateId) {
        log.info("Activating shipping rate: {}", rateId);
    }

    @Override
    public void deactivateShippingRate(String rateId) {
        log.info("Deactivating shipping rate: {}", rateId);
    }

    @Override
    public ShippingRateDTO getShippingRateByDistrict(String district, String city) {
        log.info("Fetching shipping rate for district: {}, city: {}", district, city);
        return null;
    }

    @Override
    public List<ShippingRateDTO> getAllActiveShippingRates() {
        log.info("Fetching all active shipping rates");
        List<ShippingRate> rates = shippingRateRepository.findAll();
        return rates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ShippingRateDTO convertToDTO(ShippingRate rate) {
        return ShippingRateDTO.builder()
                .rateId(rate.getRateId().toString())
                .district(rate.getBranch() != null ? rate.getBranch().getDistrict() : "")
                .city(rate.getBranch() != null ? rate.getBranch().getCity() : "")
                .baseRate(rate.getBaseFee())
                .perKmRate(rate.getPerKmFee())
                .maxDistance(rate.getFreeShipThreshold())
                .isActive(true)
                .build();
    }
}

