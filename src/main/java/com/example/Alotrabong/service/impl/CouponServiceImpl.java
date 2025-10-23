package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Coupon;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.CouponRepository;
import com.example.Alotrabong.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        log.info("Creating coupon: {}", coupon.getCode());
        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon created successfully: {}", savedCoupon.getCouponId());
        return savedCoupon;
    }

    @Override
    public Coupon updateCoupon(String couponId, Coupon coupon) {
        log.info("Updating coupon: {}", couponId);
        Coupon existingCoupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        existingCoupon.setCode(coupon.getCode());
        existingCoupon.setDescription(coupon.getDescription());
        existingCoupon.setDiscountType(coupon.getDiscountType());
        existingCoupon.setDiscountValue(coupon.getDiscountValue());
        existingCoupon.setMinOrderAmount(coupon.getMinOrderAmount());
        existingCoupon.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
        existingCoupon.setStartDate(coupon.getStartDate());
        existingCoupon.setEndDate(coupon.getEndDate());
        existingCoupon.setUsageLimit(coupon.getUsageLimit());
        
        existingCoupon = couponRepository.save(existingCoupon);
        log.info("Coupon updated successfully: {}", couponId);
        return existingCoupon;
    }

    @Override
    public void deleteCoupon(String couponId) {
        log.info("Deleting coupon: {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        coupon.setIsActive(false);
        couponRepository.save(coupon);
        
        log.info("Coupon deactivated: {}", couponId);
    }
}
