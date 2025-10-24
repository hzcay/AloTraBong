package com.example.Alotrabong.config;

import com.example.Alotrabong.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final OtpService otpService;

    @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
    public void cleanupExpiredOtps() {
        log.info("Starting cleanup of expired OTPs...");
        otpService.cleanupExpiredOtps();
        log.info("Completed cleanup of expired OTPs");
    }
}
