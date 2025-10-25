package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.LoginRequest;
import com.example.Alotrabong.dto.LoginResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.entity.OtpPurpose;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.RoleRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import com.example.Alotrabong.repository.UserOtpRepository;
import com.example.Alotrabong.service.UserService;
import com.example.Alotrabong.service.OtpService;
import com.example.Alotrabong.config.JwtTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    @Override
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists and is active
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getIsActive()) {
                throw new BadRequestException("Email already exists and is verified");
            } else {
                // User exists but not verified, update existing user
                log.info("Updating unverified user: {}", user.getEmail());
                
                // Update user information
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());
                user.setPhone(request.getPhone());
                user.setIsActive(false); // Still not activated
                
                // Delete old OTPs for this user
                userOtpRepository.deleteByUserAndPurpose(user, OtpPurpose.SIGNUP);
            }
        } else {
            // Create new user
            user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .isActive(false) // Will be activated after OTP verification
                    .build();
        }

        user = userRepository.save(user);
        
        // Assign USER role to new user only (not for existing unverified users)
        if (existingUser.isEmpty()) {
            Role userRole = roleRepository.findByRoleCode(RoleCode.USER)
                    .orElseThrow(() -> new RuntimeException("USER role not found in database"));
            
            UserRole userRoleEntity = UserRole.builder()
                    .user(user)
                    .role(userRole)
                    .build();
            userRoleRepository.save(userRoleEntity);
        }
        
        // Generate and send OTP
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

        if (!user.getIsActive()) {
            // User chưa xác thực -> gửi lại OTP
            log.info("User not activated, sending new OTP to: {}", user.getEmail());
            
            // Xóa OTP cũ nếu có
            userOtpRepository.deleteByUserAndPurpose(user, OtpPurpose.SIGNUP);
            
            // Tạo và gửi OTP mới
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

        // Verify OTP using OtpService
        if (!otpService.verifyOtp(email, otp, OtpPurpose.SIGNUP)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        // Activate the user
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

        // Generate and send OTP for password reset
        var userOtp = otpService.createOtp(user, OtpPurpose.RESET_PWD);
        otpService.sendOtpEmail(user.getEmail(), userOtp.getOtpCode(), OtpPurpose.RESET_PWD);
        
        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    public UserDTO resetPassword(String email, String otp, String newPassword) {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify OTP for password reset
        if (!otpService.verifyOtp(email, otp, OtpPurpose.RESET_PWD)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);
        
        log.info("Password reset successfully for: {}", email);
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllManagers() {
        return userRepository.findAllManagers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        
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

    private Collection<? extends GrantedAuthority> getAuthoritiesSafely(User user) {
        try {
            // Use a separate query to avoid lazy loading issues
            List<UserRole> userRoles = userRoleRepository.findByUser(user);
            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleCode().name()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error loading authorities for user, returning default USER role: {}", e.getMessage());
            // Return default USER role if there's an error
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            // Get authorities safely - use a simple approach to avoid lazy loading issues
            Collection<? extends GrantedAuthority> authorities = getAuthoritiesSafely(user);

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(!user.getIsActive())
                    .credentialsExpired(false)
                    .disabled(!user.getIsActive())
                    .build();
        } catch (Exception e) {
            log.error("Error loading user by username: {}", email, e);
            throw new UsernameNotFoundException("Error loading user: " + email, e);
        }
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<UserRole> userRoles) {
        try {
            return userRoles.stream()
                    .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getRoleCode().name()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error loading authorities for user, returning empty list: {}", e.getMessage());
            return List.of();
        }
    }

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
