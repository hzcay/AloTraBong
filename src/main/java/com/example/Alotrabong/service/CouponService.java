package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.CouponLiteDTO;
import com.example.Alotrabong.dto.CouponValidationResult;
import com.example.Alotrabong.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {

    List<Coupon> getActiveCoupons();

    Coupon getCouponByCode(String code);

    Coupon createCoupon(Coupon coupon);

    Coupon updateCoupon(String couponId, Coupon coupon);

    void deleteCoupon(String couponId);

    List<CouponLiteDTO> findEligible(String userLogin, String branchId, BigDecimal subtotal);

    CouponValidationResult validateAndPreview(String userLogin, String branchId, String code, BigDecimal subtotal);
}