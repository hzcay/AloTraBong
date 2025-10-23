package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.LoginRequest;
import com.example.Alotrabong.dto.LoginResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful. Please check your email for OTP.", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP")
    public ResponseEntity<ApiResponse<UserDTO>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        UserDTO user = userService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success("Account verified successfully", user));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent to your email", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with OTP")
    public ResponseEntity<ApiResponse<UserDTO>> resetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        UserDTO user = userService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", user));
    }
}
