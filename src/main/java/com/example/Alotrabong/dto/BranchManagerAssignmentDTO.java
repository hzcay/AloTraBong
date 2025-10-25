package com.example.Alotrabong.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchManagerAssignmentDTO {
    private String branchId;
    private String branchName;
    private String userId;
    private String userName;
    private String userEmail;
}

