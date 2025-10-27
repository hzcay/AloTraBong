package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.CouponDTO;
import com.example.Alotrabong.service.AdminCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Coupon Management", description = "APIs for managing coupons")
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping
    @Operation(summary = "Get all coupons with pagination and search")
    public ResponseEntity<Page<CouponDTO>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.info("Fetching all coupons - page: {}, size: {}, search: {}", page, size, search);
        Pageable pageable = PageRequest.of(page, size);
        Page<CouponDTO> coupons = adminCouponService.getAllCoupons(pageable, search);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all coupons without pagination")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        log.info("Fetching all coupons");
        List<CouponDTO> coupons = adminCouponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{couponId}")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable String couponId) {
        log.info("Fetching coupon by id: {}", couponId);
        CouponDTO coupon = adminCouponService.getCouponById(couponId);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code")
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable String code) {
        log.info("Fetching coupon by code: {}", code);
        CouponDTO coupon = adminCouponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping
    @Operation(summary = "Create new coupon")
    public ResponseEntity<CouponDTO> createCoupon(@RequestBody CouponDTO dto) {
        log.info("Creating new coupon: {}", dto.getCode());
        CouponDTO coupon = adminCouponService.createCoupon(dto);
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/{couponId}")
    @Operation(summary = "Update coupon")
    public ResponseEntity<CouponDTO> updateCoupon(
            @PathVariable String couponId,
            @RequestBody CouponDTO dto) {
        log.info("Updating coupon: {}", couponId);
        CouponDTO coupon = adminCouponService.updateCoupon(couponId, dto);
        return ResponseEntity.ok(coupon);
    }

    @DeleteMapping("/{couponId}")
    @Operation(summary = "Delete coupon")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable String couponId) {
        log.info("Deleting coupon: {}", couponId);
        adminCouponService.deleteCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/activate")
    @Operation(summary = "Activate coupon")
    public ResponseEntity<Map<String, String>> activateCoupon(@PathVariable String couponId) {
        log.info("Activating coupon: {}", couponId);
        adminCouponService.activateCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/deactivate")
    @Operation(summary = "Deactivate coupon")
    public ResponseEntity<Map<String, String>> deactivateCoupon(@PathVariable String couponId) {
        log.info("Deactivating coupon: {}", couponId);
        adminCouponService.deactivateCoupon(couponId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coupon deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get coupon statistics")
    public ResponseEntity<Map<String, Object>> getCouponStatistics() {
        log.info("Fetching coupon statistics");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCoupons", adminCouponService.getTotalCouponsCount());
        stats.put("activeCoupons", adminCouponService.getActiveCouponsCount());
        stats.put("expiringCoupons", adminCouponService.getExpiringCouponsCount());
        stats.put("expiredCoupons", adminCouponService.getExpiredCouponsCount());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active coupons")
    public ResponseEntity<List<CouponDTO>> getActiveCoupons() {
        log.info("Fetching active coupons");
        List<CouponDTO> coupons = adminCouponService.getActiveCoupons();
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/auto-deactivate-expired")
    @Operation(summary = "Auto-deactivate expired coupons")
    public ResponseEntity<Map<String, Object>> autoDeactivateExpiredCoupons() {
        log.info("Auto-deactivating expired coupons");
        adminCouponService.autoDeactivateExpiredCoupons();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Expired coupons have been auto-deactivated");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {
        log.info("Validating coupon: {} for order total: {}", code, orderTotal);
        boolean isValid = adminCouponService.validateCoupon(code, orderTotal);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("code", code);
        response.put("orderTotal", orderTotal);
        
        if (isValid) {
            CouponDTO coupon = adminCouponService.getCouponByCode(code);
            response.put("coupon", coupon);
        }
        
        return ResponseEntity.ok(response);
    }
}
