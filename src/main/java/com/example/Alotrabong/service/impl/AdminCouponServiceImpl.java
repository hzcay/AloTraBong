package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.CouponDTO;
import com.example.Alotrabong.entity.Coupon;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.CouponRepository;
import com.example.Alotrabong.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminCouponServiceImpl implements AdminCouponService {

    private final CouponRepository couponRepository;

    @Override
    public Page<CouponDTO> getAllCoupons(Pageable pageable, String search) {
        log.info("Fetching all coupons with search: {}", search);
        Page<Coupon> coupons;
        
        if (search != null && !search.trim().isEmpty()) {
            coupons = couponRepository.findByCodeContainingIgnoreCase(search, pageable);
        } else {
            coupons = couponRepository.findAll(pageable);
        }
        
        return coupons.map(this::convertToDTO);
    }

    @Override
    public List<CouponDTO> getAllCoupons() {
        log.info("Fetching all coupons");
        List<Coupon> coupons = couponRepository.findAll();
        return coupons.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public CouponDTO getCouponById(String couponId) {
        log.info("Fetching coupon by id: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));
        return convertToDTO(coupon);
    }

    @Override
    public CouponDTO getCouponByCode(String code) {
        log.info("Fetching coupon by code: {}", code);
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + code));
        return convertToDTO(coupon);
    }

    @Override
    public CouponDTO createCoupon(CouponDTO dto) {
        log.info("Creating new coupon: {}", dto.getCode());
        
        // Check if code already exists
        if (couponRepository.findByCodeAndIsActiveTrue(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists: " + dto.getCode());
        }
        
        Coupon coupon = Coupon.builder()
                .code(dto.getCode())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .minOrderAmount(dto.getMinOrderAmount())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        coupon = couponRepository.save(coupon);
        return convertToDTO(coupon);
    }

    @Override
    public CouponDTO updateCoupon(String couponId, CouponDTO dto) {
        log.info("Updating coupon: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

        // Check if new code already exists (excluding current coupon)
        if (!coupon.getCode().equals(dto.getCode()) && 
            couponRepository.findByCodeAndIsActiveTrue(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Coupon code already exists: " + dto.getCode());
        }

        coupon.setCode(dto.getCode());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderAmount(dto.getMinOrderAmount());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setIsActive(dto.getIsActive());

        coupon = couponRepository.save(coupon);
        return convertToDTO(coupon);
    }

    @Override
    public void deleteCoupon(String couponId) {
        log.info("Deleting coupon: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));
        couponRepository.delete(coupon);
    }

    @Override
    public void activateCoupon(String couponId) {
        log.info("Activating coupon: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));
        coupon.setIsActive(true);
        couponRepository.save(coupon);
    }

    @Override
    public void deactivateCoupon(String couponId) {
        log.info("Deactivating coupon: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));
        coupon.setIsActive(false);
        couponRepository.save(coupon);
    }

    @Override
    public long getTotalCouponsCount() {
        return couponRepository.count();
    }

    @Override
    public long getActiveCouponsCount() {
        // Auto-deactivate expired coupons first
        autoDeactivateExpiredCoupons();
        
        LocalDateTime now = LocalDateTime.now();
        
        List<Coupon> activeCoupons = couponRepository.findByIsActiveTrue();
        return activeCoupons.stream()
                .filter(coupon -> {
                    LocalDateTime endDate = coupon.getEndDate();
                    return endDate == null || endDate.isAfter(now);
                })
                .count();
    }

    @Override
    public long getExpiringCouponsCount() {
        // Auto-deactivate expired coupons first
        autoDeactivateExpiredCoupons();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next7Days = now.plusDays(7);
        
        List<Coupon> activeCoupons = couponRepository.findByIsActiveTrue();
        return activeCoupons.stream()
                .filter(coupon -> {
                    LocalDateTime endDate = coupon.getEndDate();
                    return endDate != null && 
                           endDate.isAfter(now) && 
                           endDate.isBefore(next7Days);
                })
                .count();
    }

    @Override
    public long getExpiredCouponsCount() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Coupon> allCoupons = couponRepository.findAll();
        return allCoupons.stream()
                .filter(coupon -> {
                    LocalDateTime endDate = coupon.getEndDate();
                    return endDate != null && endDate.isBefore(now);
                })
                .count();
    }

    @Override
    public List<CouponDTO> getActiveCoupons() {
        log.info("Fetching active coupons");
        List<Coupon> coupons = couponRepository.findByIsActiveTrue();
        return coupons.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void autoDeactivateExpiredCoupons() {
        log.info("Auto-deactivating expired coupons");
        LocalDateTime now = LocalDateTime.now();
        
        List<Coupon> activeCoupons = couponRepository.findByIsActiveTrue();
        List<Coupon> expiredCoupons = activeCoupons.stream()
                .filter(coupon -> {
                    LocalDateTime endDate = coupon.getEndDate();
                    return endDate != null && endDate.isBefore(now);
                })
                .collect(Collectors.toList());
        
        if (!expiredCoupons.isEmpty()) {
            log.info("Found {} expired coupons to deactivate", expiredCoupons.size());
            expiredCoupons.forEach(coupon -> {
                coupon.setIsActive(false);
                log.info("Auto-deactivated expired coupon: {} (expired at: {})", coupon.getCode(), coupon.getEndDate());
            });
            couponRepository.saveAll(expiredCoupons);
        } else {
            log.debug("No expired coupons found to deactivate");
        }
    }

    @Override
    public boolean validateCoupon(String code, BigDecimal orderTotal) {
        log.info("Validating coupon: {} for order total: {}", code, orderTotal);
        
        try {
            CouponDTO coupon = getCouponByCode(code);
            LocalDateTime now = LocalDateTime.now();
            
            // Check if coupon is active
            if (coupon.getIsActive() == null || !coupon.getIsActive()) {
                return false;
            }
            
            // Check if coupon is within valid time range
            if ((coupon.getStartDate() != null && now.isBefore(coupon.getStartDate())) ||
                (coupon.getEndDate() != null && now.isAfter(coupon.getEndDate()))) {
                return false;
            }
            
            // Check minimum order total
            if (coupon.getMinOrderAmount() != null && orderTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                return false;
            }
            
            // TODO: Check usage limits when usage tracking is implemented
            
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private CouponDTO convertToDTO(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = coupon.getStartDate();
        LocalDateTime end = coupon.getEndDate();
        boolean isExpired = end != null && now.isAfter(end);
        boolean isActiveNow = Boolean.TRUE.equals(coupon.getIsActive())
                && (start == null || !now.isBefore(start))
                && (end == null || !now.isAfter(end));
        
        return CouponDTO.builder()
                .couponId(coupon.getCouponId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .startDate(start)
                .endDate(end)
                .usageLimit(coupon.getUsageLimit())
                .isActive(coupon.getIsActive())
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .currentUsageCount(0) // TODO: Implement usage tracking
                .isExpired(isExpired)
                .isActiveNow(isActiveNow)
                .build();
    }
}
