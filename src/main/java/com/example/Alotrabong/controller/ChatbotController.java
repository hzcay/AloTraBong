package com.example.Alotrabong.controller;

import com.example.Alotrabong.service.GeminiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final GeminiChatService geminiChatService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message không được để trống"));
        }
        
        try {
            String response = geminiChatService.chat(message);
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in chatbot chat: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Chatbot AI đang hoạt động tốt"));
    }
}

