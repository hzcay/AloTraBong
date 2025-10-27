package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.CouponDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface AdminCouponService {

    // Coupon Management
    Page<CouponDTO> getAllCoupons(Pageable pageable, String search);

    List<CouponDTO> getAllCoupons();

    CouponDTO getCouponById(String couponId);

    CouponDTO getCouponByCode(String code);

    CouponDTO createCoupon(CouponDTO dto);

    CouponDTO updateCoupon(String couponId, CouponDTO dto);

    void deleteCoupon(String couponId);

    void activateCoupon(String couponId);

    void deactivateCoupon(String couponId);

    long getTotalCouponsCount();

    long getActiveCouponsCount();

    long getExpiringCouponsCount();

    long getExpiredCouponsCount();

    List<CouponDTO> getActiveCoupons();

    void autoDeactivateExpiredCoupons();

    boolean validateCoupon(String code, BigDecimal orderTotal);
}
