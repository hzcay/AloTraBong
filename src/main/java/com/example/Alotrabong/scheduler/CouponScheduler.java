package com.example.Alotrabong.scheduler;

import com.example.Alotrabong.service.AdminCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final AdminCouponService adminCouponService;

    /**
     * Tự động tắt các coupons đã hết hạn
     * Chạy mỗi ngày lúc 00:00 (midnight)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoDeactivateExpiredCoupons() {
        log.info("Starting scheduled task: Auto-deactivate expired coupons");
        try {
            adminCouponService.autoDeactivateExpiredCoupons();
            log.info("Scheduled task completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled task: Auto-deactivate expired coupons", e);
        }
    }

    /**
     * Tự động tắt các coupons đã hết hạn
     * Chạy mỗi giờ để kiểm tra thường xuyên hơn
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyCheckExpiredCoupons() {
        log.debug("Hourly check: Auto-deactivate expired coupons");
        try {
            adminCouponService.autoDeactivateExpiredCoupons();
        } catch (Exception e) {
            log.error("Error in hourly check: Auto-deactivate expired coupons", e);
        }
    }
}
