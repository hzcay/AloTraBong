package com.example.Alotrabong.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatSendMessageRequest {
    @Schema(description = "Nội dung tin nhắn", example = "Xin chào")
    private String content;

    @Schema(description = "URL media (tùy chọn)")
    private String mediaUrl;
}


