package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ShipperDTO;
import com.example.Alotrabong.dto.ShippingRateDTO;
import com.example.Alotrabong.service.AdminShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shipping")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Shipping Management", description = "APIs for managing shipping rates and shippers")
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
public class AdminShippingController {

    private final AdminShippingService adminShippingService;

    // ===== SHIPPING RATE ENDPOINTS =====

    @GetMapping("/rates")
    @Operation(summary = "Get all shipping rates")
    public ResponseEntity<List<ShippingRateDTO>> getAllShippingRates() {
        log.info("Fetching all shipping rates");
        List<ShippingRateDTO> rates = adminShippingService.getAllShippingRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/rates/{rateId}")
    @Operation(summary = "Get shipping rate by ID")
    public ResponseEntity<ShippingRateDTO> getShippingRateById(@PathVariable Integer rateId) {
        log.info("Fetching shipping rate by id: {}", rateId);
        ShippingRateDTO rate = adminShippingService.getShippingRateById(rateId);
        return ResponseEntity.ok(rate);
    }

    @GetMapping("/rates/branch/{branchId}")
    @Operation(summary = "Get shipping rate by branch ID")
    public ResponseEntity<ShippingRateDTO> getShippingRateByBranchId(@PathVariable String branchId) {
        log.info("Fetching shipping rate by branch id: {}", branchId);
        ShippingRateDTO rate = adminShippingService.getShippingRateByBranchId(branchId);
        return ResponseEntity.ok(rate);
    }

    @PostMapping("/rates")
    @Operation(summary = "Create new shipping rate")
    public ResponseEntity<ShippingRateDTO> createShippingRate(@RequestBody ShippingRateDTO dto) {
        log.info("Creating new shipping rate for branch: {}", dto.getBranchId());
        ShippingRateDTO rate = adminShippingService.createShippingRate(dto);
        return ResponseEntity.ok(rate);
    }

    @PutMapping("/rates/{rateId}")
    @Operation(summary = "Update shipping rate")
    public ResponseEntity<ShippingRateDTO> updateShippingRate(
            @PathVariable Integer rateId,
            @RequestBody ShippingRateDTO dto) {
        log.info("Updating shipping rate: {}", rateId);
        ShippingRateDTO rate = adminShippingService.updateShippingRate(rateId, dto);
        return ResponseEntity.ok(rate);
    }

    @DeleteMapping("/rates/{rateId}")
    @Operation(summary = "Delete shipping rate")
    public ResponseEntity<Map<String, String>> deleteShippingRate(@PathVariable Integer rateId) {
        log.info("Deleting shipping rate: {}", rateId);
        adminShippingService.deleteShippingRate(rateId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipping rate deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rates/{rateId}/activate")
    @Operation(summary = "Activate shipping rate")
    public ResponseEntity<Map<String, String>> activateShippingRate(@PathVariable Integer rateId) {
        log.info("Activating shipping rate: {}", rateId);
        adminShippingService.activateShippingRate(rateId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipping rate activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rates/{rateId}/deactivate")
    @Operation(summary = "Deactivate shipping rate")
    public ResponseEntity<Map<String, String>> deactivateShippingRate(@PathVariable Integer rateId) {
        log.info("Deactivating shipping rate: {}", rateId);
        adminShippingService.deactivateShippingRate(rateId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipping rate deactivated successfully");
        return ResponseEntity.ok(response);
    }

    // ===== SHIPPER ENDPOINTS =====

    @GetMapping("/shippers")
    @Operation(summary = "Get all shippers")
    public ResponseEntity<List<ShipperDTO>> getAllShippers() {
        log.info("Fetching all shippers");
        List<ShipperDTO> shippers = adminShippingService.getAllShippers();
        return ResponseEntity.ok(shippers);
    }

    @GetMapping("/shippers/active")
    @Operation(summary = "Get active shippers")
    public ResponseEntity<List<ShipperDTO>> getActiveShippers() {
        log.info("Fetching active shippers");
        List<ShipperDTO> shippers = adminShippingService.getActiveShippers();
        return ResponseEntity.ok(shippers);
    }

    @GetMapping("/shippers/{shipperId}")
    @Operation(summary = "Get shipper by ID")
    public ResponseEntity<ShipperDTO> getShipperById(@PathVariable String shipperId) {
        log.info("Fetching shipper by id: {}", shipperId);
        ShipperDTO shipper = adminShippingService.getShipperById(shipperId);
        return ResponseEntity.ok(shipper);
    }

    @PostMapping("/shippers")
    @Operation(summary = "Create new shipper")
    public ResponseEntity<ShipperDTO> createShipper(@RequestBody ShipperDTO dto) {
        log.info("Creating new shipper for user: {}", dto.getUserId());
        ShipperDTO shipper = adminShippingService.createShipper(dto);
        return ResponseEntity.ok(shipper);
    }

    @PutMapping("/shippers/{shipperId}")
    @Operation(summary = "Update shipper")
    public ResponseEntity<ShipperDTO> updateShipper(
            @PathVariable String shipperId,
            @RequestBody ShipperDTO dto) {
        log.info("Updating shipper: {}", shipperId);
        ShipperDTO shipper = adminShippingService.updateShipper(shipperId, dto);
        return ResponseEntity.ok(shipper);
    }

    @DeleteMapping("/shippers/{shipperId}")
    @Operation(summary = "Delete shipper")
    public ResponseEntity<Map<String, String>> deleteShipper(@PathVariable String shipperId) {
        log.info("Deleting shipper: {}", shipperId);
        adminShippingService.deleteShipper(shipperId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipper deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shippers/{shipperId}/activate")
    @Operation(summary = "Activate shipper")
    public ResponseEntity<Map<String, String>> activateShipper(@PathVariable String shipperId) {
        log.info("Activating shipper: {}", shipperId);
        adminShippingService.activateShipper(shipperId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipper activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/shippers/{shipperId}/deactivate")
    @Operation(summary = "Deactivate shipper")
    public ResponseEntity<Map<String, String>> deactivateShipper(@PathVariable String shipperId) {
        log.info("Deactivating shipper: {}", shipperId);
        adminShippingService.deactivateShipper(shipperId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipper deactivated successfully");
        return ResponseEntity.ok(response);
    }

    // ===== STATISTICS ENDPOINTS =====

    @GetMapping("/statistics")
    @Operation(summary = "Get shipping statistics")
    public ResponseEntity<Map<String, Object>> getShippingStatistics() {
        log.info("Fetching shipping statistics");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShippingRates", adminShippingService.getTotalShippingRatesCount());
        stats.put("activeShippingRates", adminShippingService.getActiveShippingRatesCount());
        stats.put("totalShippers", adminShippingService.getTotalShippersCount());
        stats.put("activeShippers", adminShippingService.getActiveShippersCount());
        return ResponseEntity.ok(stats);
    }
}
