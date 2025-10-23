package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.ShipperRepository;
import com.example.Alotrabong.service.ShipperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipperServiceImpl implements ShipperService {

    private final ShipperRepository shipperRepository;

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
        log.info("Creating shipper: {}", shipper.getName());
        Shipper savedShipper = shipperRepository.save(shipper);
        log.info("Shipper created successfully: {}", savedShipper.getShipperId());
        return savedShipper;
    }

    @Override
    public Shipper updateShipper(String shipperId, Shipper shipper) {
        log.info("Updating shipper: {}", shipperId);
        Shipper existingShipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found"));
        
        existingShipper.setName(shipper.getName());
        existingShipper.setPhone(shipper.getPhone());
        existingShipper.setEmail(shipper.getEmail());
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
}
