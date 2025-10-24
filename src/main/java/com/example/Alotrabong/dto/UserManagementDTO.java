package com.example.Alotrabong.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserManagementDTO {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private List<String> roles;
    private String branchId;
}

