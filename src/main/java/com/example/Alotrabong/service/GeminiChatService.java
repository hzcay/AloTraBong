package com.example.Alotrabong.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.base-url}")
    private String geminiApiBaseUrl;

    @Value("${gemini.api.model}")
    private String geminiModel;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    @Value("${gemini.api.temperature}")
    private double temperature;

    private final ContextService contextService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String userMessage) {
        try {
            // Lấy context từ database
            String context = contextService.getPublicContext();
            
            // Tạo system prompt cho AI agent
            String systemPrompt = buildSystemPrompt(context);
            
            // Xây dựng request
            Map<String, Object> request = buildRequest(systemPrompt, userMessage);
            
            // Gọi Gemini API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            String url = geminiApiBaseUrl + "/models/" + geminiModel + ":generateContent?key=" + geminiApiKey;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            return extractResponse(jsonResponse);
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Gemini API HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.";
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.";
        }
    }

    private String buildSystemPrompt(String context) {
        return String.format("""
                Bạn là trợ lý AI thân thiện của nhà hàng AloTraBong — một quán cơm bình dân nổi tiếng, vibe ấm cúng và cực chill 🍚✨
                
                NHIỆM VỤ CỦA BẠN:
                1. Giúp khách tìm hiểu thực đơn, món ăn, combo, khuyến mãi hot 🔥
                2. Gợi ý món ngon theo khẩu vị hoặc mood của khách (ví dụ: "nay hơi mệt" → món nhẹ bụng; "đang đói lả" → cơm sườn full topping 😋)
                3. Trả lời mượt mà về giá, thành phần, khẩu phần, dịch vụ
                4. Hướng dẫn khách đặt món, thanh toán, hoặc lấy hóa đơn một cách dễ hiểu
                5. Giữ vibe vui vẻ, nhiệt tình, thân thiện, kiểu "nhân viên quán ruột" — nhưng vẫn rõ ràng và chuyên nghiệp
                
                LUẬT CHƠI:
                - Chỉ trả lời dựa trên dữ liệu được cung cấp bên dưới
                - KHÔNG tự bịa thông tin ngoài database 🚫
                - KHÔNG hỏi hay thu thập thông tin cá nhân (email, sđt, địa chỉ,…)
                - KHÔNG xử lý vấn đề thanh toán, tài khoản, bảo mật
                - Luôn dùng tiếng Việt tự nhiên, hơi Gen Z, thêm emoji nhẹ nhàng khi hợp bối cảnh 😄
                
                DỮ LIỆU HIỆN CÓ:
                %s
                
                🔥 Bắt đầu trò chuyện với khách đi — thân thiện, tự tin và chill nhé!
                """, context);
    }

    private Map<String, Object> buildRequest(String systemPrompt, String userMessage) {
        Map<String, Object> request = new HashMap<>();
        
        // Contents array - only current user message
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("role", "user", "parts", Arrays.asList(Map.of("text", userMessage))));
        
        request.put("contents", contents);
        
        // System instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        systemInstruction.put("parts", Arrays.asList(Map.of("text", systemPrompt)));
        request.put("system_instruction", systemInstruction);
        
        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxOutputTokens",4000);
        generationConfig.put("temperature", temperature);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);
        request.put("generationConfig", generationConfig);
        
        return request;
    }

    private String extractResponse(JsonNode jsonResponse) {
        try {
            JsonNode candidates = jsonResponse.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode candidate = candidates.get(0);
                JsonNode content = candidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode textPart = parts.get(0);
                        return textPart.get("text").asText();
                    }
                }
            }
            return "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.";
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            return "Có lỗi xảy ra khi xử lý phản hồi.";
        }
    }
}

