package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.LoginRequest;
import com.example.Alotrabong.dto.LoginResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.service.UserService;
import com.example.Alotrabong.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserDTO register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isActive(false) // Will be activated after OTP verification
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getUserId());

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
            throw new BadRequestException("Account not activated. Please verify your email first.");
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

        // TODO: Implement OTP verification logic
        // For now, just activate the user
        user.setIsActive(true);
        user = userRepository.save(user);
        
        log.info("User account activated: {}", email);
        return convertToDTO(user);
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);
        
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Implement OTP generation and email sending
        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    public UserDTO resetPassword(String email, String otp, String newPassword) {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Implement OTP verification
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
