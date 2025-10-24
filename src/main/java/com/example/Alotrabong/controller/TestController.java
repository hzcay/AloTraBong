package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Test APIs")
public class TestController {

    private final UserService userService;

    @PostMapping("/register-twice")
    @Operation(summary = "Test registering same email twice")
    public ResponseEntity<ApiResponse<String>> testRegisterTwice() {
        try {
            // First registration
            RegisterRequest request1 = new RegisterRequest();
            request1.setEmail("test@example.com");
            request1.setPassword("password123");
            request1.setFullName("Test User");
            request1.setPhone("0123456789");
            
            userService.register(request1);
            
            // Second registration with same email (should work because first user is not verified)
            RegisterRequest request2 = new RegisterRequest();
            request2.setEmail("test@example.com");
            request2.setPassword("newpassword123");
            request2.setFullName("Test User Updated");
            request2.setPhone("0987654321");
            
            UserDTO user2 = userService.register(request2);
            
            return ResponseEntity.ok(ApiResponse.success("Second registration successful", 
                "First user deleted, second user created with ID: " + user2.getUserId()));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Test failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login-unverified")
    @Operation(summary = "Test login with unverified account")
    public ResponseEntity<ApiResponse<String>> testLoginUnverified() {
        try {
            // First register a user (unverified)
            RegisterRequest request = new RegisterRequest();
            request.setEmail("unverified@example.com");
            request.setPassword("password123");
            request.setFullName("Unverified User");
            request.setPhone("0123456789");
            
            userService.register(request);
            
            // Try to login (should send new OTP)
            com.example.Alotrabong.dto.LoginRequest loginRequest = new com.example.Alotrabong.dto.LoginRequest();
            loginRequest.setEmail("unverified@example.com");
            loginRequest.setPassword("password123");
            
            try {
                userService.login(loginRequest);
                return ResponseEntity.ok(ApiResponse.success("Login successful", "This should not happen"));
            } catch (Exception e) {
                return ResponseEntity.ok(ApiResponse.success("Expected behavior", 
                    "Login failed as expected: " + e.getMessage()));
            }
                
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Test failed: " + e.getMessage()));
        }
    }
}
