package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.LoginRequest;
import com.example.Alotrabong.dto.LoginResponse;
import com.example.Alotrabong.dto.RegisterRequest;
import com.example.Alotrabong.dto.UserDTO;

import java.util.List;

public interface UserService {
    
    UserDTO register(RegisterRequest request);
    
    LoginResponse login(LoginRequest request);
    
    UserDTO verifyOtp(String email, String otp);
    
    void forgotPassword(String email);
    
    UserDTO resetPassword(String email, String otp, String newPassword);
    
    UserDTO getUserById(String userId);
    
    UserDTO getUserByEmail(String email);
    
    List<UserDTO> getAllUsers();
    
    UserDTO updateUser(String userId, UserDTO userDTO);
    
    void deleteUser(String userId);
    
    UserDTO activateUser(String userId);
    
    UserDTO deactivateUser(String userId);
}