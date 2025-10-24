package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserOtp;
import com.example.Alotrabong.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {
    
    Optional<UserOtp> findByUserAndPurposeAndIsUsedFalseAndExpiresAtAfter(
            User user, OtpPurpose purpose, LocalDateTime now);
    
    Optional<UserOtp> findByUserAndOtpCodeAndPurposeAndIsUsedFalseAndExpiresAtAfter(
            User user, String otpCode, OtpPurpose purpose, LocalDateTime now);
    
    @Query("SELECT uo FROM UserOtp uo WHERE uo.user.email = :email AND uo.purpose = :purpose AND uo.isUsed = false AND uo.expiresAt > :now ORDER BY uo.createdAt DESC")
    Optional<UserOtp> findLatestByEmailAndPurpose(@Param("email") String email, @Param("purpose") OtpPurpose purpose, @Param("now") LocalDateTime now);
    
    List<UserOtp> findByUserAndPurpose(User user, OtpPurpose purpose);
    
    void deleteByUserAndPurpose(User user, OtpPurpose purpose);
    
    void deleteByUser(User user);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}
