package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.config.JwtTokenProvider;
import com.example.Alotrabong.dto.LoginRequest;
import com.example.Alotrabong.dto.LoginResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.dto.UserProfileFormDTO;
import com.example.Alotrabong.entity.OtpPurpose;
import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.RoleRepository;
import com.example.Alotrabong.repository.UserOtpRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import com.example.Alotrabong.service.OtpService;
import com.example.Alotrabong.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    // ========= Helper =========
    private static String normalize(String s) {
        if (s == null) return null;
        return s.trim().replaceAll("\\s+", " ");
    }

    // ========= Auth & Account flows =========

    @Override
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (Boolean.TRUE.equals(user.getIsActive())) {
                throw new BadRequestException("Email already exists and is verified");
            } else {
                log.info("Updating unverified user: {}", user.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setPhone(request.getPhone());
                user.setIsActive(false);
                userOtpRepository.deleteByUserAndPurpose(user, OtpPurpose.SIGNUP);
            }
        } else {
            user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .isActive(false)
                    .build();
        }

        user = userRepository.save(user);

        if (existingUser.isEmpty()) {
            Role userRole = roleRepository.findByRoleCode(RoleCode.USER)
                    .orElseThrow(() -> new RuntimeException("USER role not found in database"));

            UserRole userRoleEntity = UserRole.builder()
                    .user(user)
                    .role(userRole)
                    .build();
            userRoleRepository.save(userRoleEntity);
        }

        var userOtp = otpService.createOtp(user, OtpPurpose.SIGNUP);
        otpService.sendOtpEmail(user.getEmail(), userOtp.getOtpCode(), OtpPurpose.SIGNUP);

        log.info("User registered successfully with ID: {} and USER role assigned, OTP sent", user.getUserId());
        return convertToDTO(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.info("User not activated, sending new OTP to: {}", user.getEmail());
            userOtpRepository.deleteByUserAndPurpose(user, OtpPurpose.SIGNUP);
            var userOtp = otpService.createOtp(user, OtpPurpose.SIGNUP);
            otpService.sendOtpEmail(user.getEmail(), userOtp.getOtpCode(), OtpPurpose.SIGNUP);
            throw new BadRequestException("Account not activated. A new verification code has been sent to your email.");
        }

        String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(convertToDTO(user));

        log.info("User logged in successfully: {}", user.getEmail());
        return response;
    }

    @Override
    public UserDTO verifyOtp(String email, String otp) {
        log.info("Verifying OTP for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!otpService.verifyOtp(email, otp, OtpPurpose.SIGNUP)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("User account activated: {}", email);
        return convertToDTO(user);
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var userOtp = otpService.createOtp(user, OtpPurpose.RESET_PWD);
        otpService.sendOtpEmail(user.getEmail(), userOtp.getOtpCode(), OtpPurpose.RESET_PWD);

        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    public UserDTO resetPassword(String email, String otp, String newPassword) {
        log.info("Resetting password for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!otpService.verifyOtp(email, otp, OtpPurpose.RESET_PWD)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);

        log.info("Password reset successfully for: {}", email);
        return convertToDTO(user);
    }

    // ========= Queries =========

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<UserDTO> getAllManagers() {
        return userRepository.findAllManagers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ========= Admin update =========

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        // Admin flow tuỳ ý có thể cho đổi email/isActive… nếu bạn muốn

        user = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return convertToDTO(user);
    }

    @Override
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
        log.info("User deleted successfully: {}", userId);
    }

    @Override
    public UserDTO activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(true);
        user = userRepository.save(user);

        log.info("User activated: {}", userId);
        return convertToDTO(user);
    }

    @Override
    public UserDTO deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        user = userRepository.save(user);

        log.info("User deactivated: {}", userId);
        return convertToDTO(user);
    }

    // ========= UserDetailsService =========

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            Collection<? extends GrantedAuthority> authorities = getAuthoritiesSafely(user);

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!Boolean.TRUE.equals(user.getIsActive()))
                    .credentialsExpired(false)
                    .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                    .build();
        } catch (Exception e) {
            log.error("Error loading user by username: {}", email, e);
            throw new UsernameNotFoundException("Error loading user: " + email, e);
        }
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesSafely(User user) {
        try {
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleCode().name()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error loading authorities for user, returning default USER role: {}", e.getMessage());
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    // ========= Profile (NEW) =========

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public UserDTO getProfile(String userId) {
        // Tận dụng sẵn getUserById
        return getUserById(userId);
    }

    @Override
    public UserDTO updateProfile(String userId, UserProfileFormDTO form) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String fullName = normalize(form.getFullName());
        String email    = normalize(form.getEmail());
        String address  = normalize(form.getAddress());
        String phone    = normalize(form.getPhone());

        if (email == null || email.isBlank())      throw new BadRequestException("Email is required");
        if (fullName == null || fullName.isBlank())throw new BadRequestException("Full name is required");
        if (address == null || address.isBlank())  throw new BadRequestException("Address is required");
        if (phone == null || phone.isBlank())      throw new BadRequestException("Phone is required");

        // Check trùng với user khác
        if (!email.equalsIgnoreCase(user.getEmail())
                && existsEmailForAnotherUser(email, userId)) {
            throw new BadRequestException("Email already in use by another account");
        }
        if (!phone.equals(user.getPhone())
                && existsPhoneForAnotherUser(phone, userId)) {
            throw new BadRequestException("Phone already in use by another account");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        user = userRepository.save(user);
        log.info("Profile updated for userId={} (email={})", userId, email);

        return convertToDTO(user);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsEmailForAnotherUser(String email, String excludeUserId) {
        return userRepository.existsByEmailIgnoreCaseAndUserIdNot(email, excludeUserId);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsPhoneForAnotherUser(String phone, String excludeUserId) {
        return userRepository.existsByPhoneAndUserIdNot(phone, excludeUserId);
    }

    // ========= Mapping =========

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
