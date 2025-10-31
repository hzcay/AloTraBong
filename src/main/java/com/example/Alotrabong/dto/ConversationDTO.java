package com.example.Alotrabong.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    private String convoId;
    private String userId;
    private String userEmail;
    private String userFullName;
    private String branchId;
    private String branchName;
    private Byte status;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

