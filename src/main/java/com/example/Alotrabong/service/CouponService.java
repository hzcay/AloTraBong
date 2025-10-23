package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Coupon;

import java.util.List;

public interface CouponService {
    
    List<Coupon> getActiveCoupons();
    
    Coupon getCouponByCode(String code);
    
    Coupon createCoupon(Coupon coupon);
    
    Coupon updateCoupon(String couponId, Coupon coupon);
    
    void deleteCoupon(String couponId);
}