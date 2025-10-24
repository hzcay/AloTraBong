package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserOtp;
import com.example.Alotrabong.entity.OtpPurpose;

public interface OtpService {
    
    String generateOtp();
    
    UserOtp createOtp(User user, OtpPurpose purpose);
    
    boolean verifyOtp(String email, String otpCode, OtpPurpose purpose);
    
    void sendOtpEmail(String email, String otpCode, OtpPurpose purpose);
    
    void cleanupExpiredOtps();
}
