package com.example.Alotrabong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private String messageId;
    private String senderUserId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
}


