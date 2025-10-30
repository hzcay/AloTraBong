package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.CouponLiteDTO;
import com.example.Alotrabong.dto.CouponValidationResult;
import com.example.Alotrabong.entity.Coupon;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.CouponRepository;
import com.example.Alotrabong.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // ====== Luồng Checkout ======

    @Override
    @Transactional(readOnly = true)
    public List<CouponLiteDTO> findEligible(String userLogin, String branchId, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> src = couponRepository.findByIsActiveTrue();

        List<CouponLiteDTO> out = new ArrayList<>();
        for (Coupon c : src) {
            if (!isWithinDate(c, now))
                continue;
            if (c.getMinOrderAmount() != null && subtotal.compareTo(c.getMinOrderAmount()) < 0)
                continue;
            // TODO: filter theo branch/user/quota nếu schema có
            out.add(toLite(c));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResult validateAndPreview(String userLogin,
            String branchId,
            String code,
            BigDecimal subtotal) {
        if (code == null || code.isBlank())
            return CouponValidationResult.fail("Mã trống.");

        Coupon c = couponRepository.findByCodeAndIsActiveTrue(code.trim()).orElse(null);
        if (c == null)
            return CouponValidationResult.fail("Mã không tồn tại hoặc đã bị khóa.");

        LocalDateTime now = LocalDateTime.now();
        if (!isWithinDate(c, now))
            return CouponValidationResult.fail("Mã đã hết hạn hoặc chưa tới ngày áp dụng.");
        if (c.getMinOrderAmount() != null && subtotal.compareTo(c.getMinOrderAmount()) < 0)
            return CouponValidationResult.fail("Đơn chưa đạt tối thiểu để dùng mã.");
        // TODO: quota/usage/user/branch nếu có

        BigDecimal discount = calcDiscount(c, subtotal); // tính theo String type
        if (discount.compareTo(BigDecimal.ZERO) <= 0)
            return CouponValidationResult.fail("Mã không tạo ra giảm giá.");

        // cap theo maxDiscountAmount nếu có
        if (c.getMaxDiscountAmount() != null) {
            discount = discount.min(zeroSafe(c.getMaxDiscountAmount()));
        }
        // không vượt subtotal
        discount = discount.min(subtotal.max(BigDecimal.ZERO));

        String msg = "Áp dụng " + c.getCode() + ": -" + formatVnd(discount);
        return CouponValidationResult.ok(discount, msg);
    }

    // ===== Helpers =====
    private static boolean isWithinDate(Coupon c, LocalDateTime now) {
        if (Boolean.FALSE.equals(c.getIsActive()))
            return false;
        if (c.getStartDate() != null && now.isBefore(c.getStartDate()))
            return false;
        if (c.getEndDate() != null && now.isAfter(c.getEndDate()))
            return false;
        return true;
    }

    private static CouponLiteDTO toLite(Coupon c) {
        String type = normType(c.getDiscountType()); // "PERCENT" | "AMOUNT"
        return new CouponLiteDTO(
                c.getCode(),
                type,
                zeroSafe(c.getDiscountValue()),
                zeroSafe(c.getMinOrderAmount()),
                c.getMaxDiscountAmount(),
                c.getEndDate());
    }

    private static BigDecimal calcDiscount(Coupon c, BigDecimal subtotal) {
        String type = normType(c.getDiscountType());
        BigDecimal value = zeroSafe(c.getDiscountValue());
        if (isPercent(type)) {
            BigDecimal raw = subtotal.multiply(value)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            return raw;
        }
        // AMOUNT (mặc định fallback)
        return value;
    }

    private static String normType(String t) {
        if (t == null)
            return "AMOUNT";
        String s = t.trim().toUpperCase();
        return ("PERCENT".equals(s) || "AMOUNT".equals(s)) ? s : "AMOUNT";
        // nếu DB có "PERCENTAGE" thì bạn map thêm ở đây
    }

    private static boolean isPercent(String t) {
        return "PERCENT".equalsIgnoreCase(t);
    }

    private static BigDecimal zeroSafe(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    private static String formatVnd(BigDecimal v) {
        return v.setScale(0, RoundingMode.FLOOR).toPlainString() + "đ";
    }
}
