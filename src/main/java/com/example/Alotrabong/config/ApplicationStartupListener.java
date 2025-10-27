package com.example.Alotrabong.config;

import com.example.Alotrabong.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener {

    private final AdminCouponService adminCouponService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Application started - Auto-deactivating expired coupons");
        try {
            adminCouponService.autoDeactivateExpiredCoupons();
            log.info("Startup coupon cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during startup coupon cleanup", e);
        }
    }
}
