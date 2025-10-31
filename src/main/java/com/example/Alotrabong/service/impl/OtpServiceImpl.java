package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserOtp;
import com.example.Alotrabong.entity.OtpPurpose;
import com.example.Alotrabong.repository.UserOtpRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final UserOtpRepository userOtpRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;

    @Override
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    @Override
    @Transactional
    public UserOtp createOtp(User user, OtpPurpose purpose) {
        // Xóa OTP cũ nếu có
        userOtpRepository.deleteByUserAndPurpose(user, purpose);

        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5); // OTP hết hạn sau 5 phút
        
        UserOtp userOtp = UserOtp.builder()
                .user(user)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        
        userOtp = userOtpRepository.save(userOtp);
        
        try {
            String redisKey = String.format("otp:%s:%s", user.getEmail(), purpose.name());
            redisTemplate.opsForValue().set(redisKey, otpCode, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis not available, OTP will be stored in database only: {}", e.getMessage());
        }
        
        log.info("Created OTP for user: {} with purpose: {}", user.getEmail(), purpose, otpCode);
        return userOtp;
    }

    @Override
    public boolean verifyOtp(String email, String otpCode, OtpPurpose purpose) {
        try {
            String redisKey = String.format("otp:%s:%s", email, purpose.name());
            String cachedOtp = redisTemplate.opsForValue().get(redisKey);
            
            if (cachedOtp != null && cachedOtp.equals(otpCode)) {
                redisTemplate.delete(redisKey);
                
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                UserOtp userOtp = userOtpRepository.findByUserAndOtpCodeAndPurposeAndIsUsedFalseAndExpiresAtAfter(
                        user, otpCode, purpose, LocalDateTime.now())
                        .orElse(null);
                
                if (userOtp != null) {
                    userOtp.setIsUsed(true);
                    userOtpRepository.save(userOtp);
                    log.info("OTP verified successfully for user: {}", email);
                    return true;
                }
            }
            
            log.warn("OTP verification failed for user: {}", email);
            return false;
        } catch (Exception e) {
            log.error("Error verifying OTP for user: {}", email, e);
            return false;
        }
    }

    @Override
    @Async
    public void sendOtpEmail(String email, String otpCode, OtpPurpose purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            
            String subject;
            String text;
            
            switch (purpose) {
                case SIGNUP:
                    subject = "Xác thực tài khoản AloTraBong";
                    text = String.format("""
                        Chào mừng bạn đến với AloTraBong!
                        
                        Mã OTP để xác thực tài khoản của bạn là: %s
                        
                        Mã này có hiệu lực trong 5 phút.
                        
                        Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.
                        
                        Trân trọng,
                        Đội ngũ AloTraBong
                        """, otpCode);
                    break;
                case RESET_PWD:
                    subject = "Đặt lại mật khẩu AloTraBong";
                    text = String.format("""
                        Bạn đã yêu cầu đặt lại mật khẩu.
                        
                        Mã OTP để đặt lại mật khẩu là: %s
                        
                        Mã này có hiệu lực trong 5 phút.
                        
                        Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                        
                        Trân trọng,
                        Đội ngũ AloTraBong
                        """, otpCode);
                    break;
                default:
                    subject = "Mã OTP từ AloTraBong";
                    text = String.format("Mã OTP của bạn là: %s", otpCode);
            }
            
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@alotrabong.com");
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", email, e);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        userOtpRepository.deleteByExpiresAtBefore(now);
        log.info("Cleaned up expired OTPs");
    }
}