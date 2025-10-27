package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ShipperDTO;
import com.example.Alotrabong.dto.ShippingRateDTO;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.entity.ShippingRate;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.repository.ShipperRepository;
import com.example.Alotrabong.repository.ShippingRateRepository;
import com.example.Alotrabong.repository.UserRepository;
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
    private final ShipperRepository shipperRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    // ===== SHIPPING RATE MANAGEMENT =====

    @Override
    public Page<ShippingRateDTO> getAllShippingRates(Pageable pageable) {
        log.info("Fetching all shipping rates with pagination");
        Page<ShippingRate> rates = shippingRateRepository.findAll(pageable);
        return rates.map(this::convertToShippingRateDTO);
    }

    @Override
    public List<ShippingRateDTO> getAllShippingRates() {
        log.info("Fetching all shipping rates");
        List<ShippingRate> rates = shippingRateRepository.findAll();
        return rates.stream().map(this::convertToShippingRateDTO).collect(Collectors.toList());
    }

    @Override
    public ShippingRateDTO getShippingRateById(Integer rateId) {
        log.info("Fetching shipping rate by id: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        return convertToShippingRateDTO(rate);
    }

    @Override
    public ShippingRateDTO getShippingRateByBranchId(String branchId) {
        log.info("Fetching shipping rate by branch id: {}", branchId);
        ShippingRate rate = shippingRateRepository.findActiveByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Active shipping rate not found for branch: " + branchId));
        return convertToShippingRateDTO(rate);
    }

    @Override
    public ShippingRateDTO createShippingRate(ShippingRateDTO dto) {
        log.info("Creating new shipping rate for branch: {}", dto.getBranchId());
        
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + dto.getBranchId()));
        
        ShippingRate rate = ShippingRate.builder()
                .branch(branch)
                .baseFee(dto.getBaseFee())
                .perKmFee(dto.getPerKmFee())
                .freeShipThreshold(dto.getFreeShipThreshold())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        rate = shippingRateRepository.save(rate);
        return convertToShippingRateDTO(rate);
    }

    @Override
    public ShippingRateDTO updateShippingRate(Integer rateId, ShippingRateDTO dto) {
        log.info("Updating shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));

        rate.setBaseFee(dto.getBaseFee());
        rate.setPerKmFee(dto.getPerKmFee());
        rate.setFreeShipThreshold(dto.getFreeShipThreshold());
        rate.setIsActive(dto.getIsActive());

        rate = shippingRateRepository.save(rate);
        return convertToShippingRateDTO(rate);
    }

    @Override
    public void deleteShippingRate(Integer rateId) {
        log.info("Deleting shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        shippingRateRepository.delete(rate);
    }

    @Override
    public void activateShippingRate(Integer rateId) {
        log.info("Activating shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        rate.setIsActive(true);
        shippingRateRepository.save(rate);
    }

    @Override
    public void deactivateShippingRate(Integer rateId) {
        log.info("Deactivating shipping rate: {}", rateId);
        ShippingRate rate = shippingRateRepository.findById(rateId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found with id: " + rateId));
        rate.setIsActive(false);
        shippingRateRepository.save(rate);
    }

    // ===== SHIPPER MANAGEMENT =====

    @Override
    public Page<ShipperDTO> getAllShippers(Pageable pageable, String search) {
        log.info("Fetching all shippers with search: {}", search);
        Page<Shipper> shippers = shipperRepository.findAll(pageable);
        return shippers.map(this::convertToShipperDTO);
    }

    @Override
    public List<ShipperDTO> getAllShippers() {
        log.info("Fetching all shippers");
        List<Shipper> shippers = shipperRepository.findAll();
        return shippers.stream().map(this::convertToShipperDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShipperDTO> getActiveShippers() {
        log.info("Fetching active shippers");
        List<Shipper> shippers = shipperRepository.findByIsActiveTrue();
        return shippers.stream().map(this::convertToShipperDTO).collect(Collectors.toList());
    }

    @Override
    public ShipperDTO getShipperById(String shipperId) {
        log.info("Fetching shipper by id: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));
        return convertToShipperDTO(shipper);
    }

    @Override
    public ShipperDTO createShipper(ShipperDTO dto) {
        log.info("Creating new shipper for user: {}", dto.getUserId());
        
        // Find user and branch
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + dto.getBranchId()));
        
        Shipper shipper = Shipper.builder()
                .user(user)
                .branch(branch)
                .vehiclePlate(dto.getVehiclePlate())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        shipper = shipperRepository.save(shipper);
        return convertToShipperDTO(shipper);
    }

    @Override
    public ShipperDTO updateShipper(String shipperId, ShipperDTO dto) {
        log.info("Updating shipper: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));

        // Update branch if changed
        if (dto.getBranchId() != null && !dto.getBranchId().equals(shipper.getBranch().getBranchId())) {
            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + dto.getBranchId()));
            shipper.setBranch(branch);
        }

        shipper.setVehiclePlate(dto.getVehiclePlate());
        shipper.setIsActive(dto.getIsActive());

        shipper = shipperRepository.save(shipper);
        return convertToShipperDTO(shipper);
    }

    @Override
    public void deleteShipper(String shipperId) {
        log.info("Deleting shipper: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));
        shipperRepository.delete(shipper);
    }

    @Override
    public void activateShipper(String shipperId) {
        log.info("Activating shipper: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));
        shipper.setIsActive(true);
        shipperRepository.save(shipper);
    }

    @Override
    public void deactivateShipper(String shipperId) {
        log.info("Deactivating shipper: {}", shipperId);
        Shipper shipper = shipperRepository.findById(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipper not found with id: " + shipperId));
        shipper.setIsActive(false);
        shipperRepository.save(shipper);
    }

    // ===== STATISTICS =====

    @Override
    public long getTotalShippingRatesCount() {
        return shippingRateRepository.count();
    }

    @Override
    public long getActiveShippingRatesCount() {
        return shippingRateRepository.countByIsActive(true);
    }

    @Override
    public long getTotalShippersCount() {
        return shipperRepository.count();
    }

    @Override
    public long getActiveShippersCount() {
        return shipperRepository.countActiveShippers();
    }

    // ===== CONVERSION METHODS =====

    private ShippingRateDTO convertToShippingRateDTO(ShippingRate rate) {
        Branch branch = rate.getBranch();
        
        return ShippingRateDTO.builder()
                .rateId(rate.getRateId())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .baseFee(rate.getBaseFee())
                .perKmFee(rate.getPerKmFee())
                .freeShipThreshold(rate.getFreeShipThreshold())
                .isActive(rate.getIsActive())
                .createdAt(rate.getCreatedAt())
                .updatedAt(rate.getUpdatedAt())
                .branchAddress(branch.getAddress())
                .branchPhone(branch.getPhone())
                .branchIsActive(branch.getIsActive())
                .build();
    }

    private ShipperDTO convertToShipperDTO(Shipper shipper) {
        User user = shipper.getUser();
        Branch branch = shipper.getBranch();
        
        return ShipperDTO.builder()
                .shipperId(shipper.getShipperId())
                .userId(user.getUserId())
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .userPhone(user.getPhone())
                .branchId(branch.getBranchId())
                .branchName(branch.getName())
                .vehiclePlate(shipper.getVehiclePlate())
                .isActive(shipper.getIsActive())
                .createdAt(shipper.getCreatedAt())
                .updatedAt(shipper.getUpdatedAt())
                .totalShipments(0) // TODO: Implement shipment counting
                .completedShipments(0) // TODO: Implement shipment counting
                .activeShipments(0) // TODO: Implement shipment counting
                .averageRating(0.0) // TODO: Implement rating calculation
                .build();
    }
}