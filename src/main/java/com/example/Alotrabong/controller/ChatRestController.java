package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Conversation;
import com.example.Alotrabong.entity.Message;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.dto.ChatMessageDTO;
import com.example.Alotrabong.repository.BranchRepository;
import com.example.Alotrabong.repository.ConversationRepository;
import com.example.Alotrabong.repository.MessageRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final UserRoleRepository userRoleRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @PostMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createOrGetConversation(@RequestBody Map<String, String> body,
                                                                       Authentication authentication) {
        String branchId = body.get("branchId");
        if (branchId == null || branchId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Tìm cuộc trò chuyện mở hiện có
        List<Conversation> mine = conversationRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId());
        Conversation convo = mine.stream()
                .filter(c -> c.getBranch() != null && branchId.equals(c.getBranch().getBranchId()) && (c.getStatus() == null || c.getStatus() == 0))
                .findFirst()
                .orElse(null);
        if (convo == null) {
            convo = Conversation.builder()
                    .user(user)
                    .branch(branch)
                    .status((byte)0)
                    .build();
            convo = conversationRepository.save(convo);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("convoId", convo.getConvoId());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/conversations/{convoId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ChatMessageDTO>> getMessages(@PathVariable String convoId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     Authentication authentication) {
        Conversation convo = conversationRepository.findById(convoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> result = messageRepository.findByConversation_ConvoIdOrderBySentAtDesc(convo.getConvoId(), pageable);
        Page<ChatMessageDTO> mapped = result.map(m -> new ChatMessageDTO(
                m.getMessageId(),
                m.getSender() != null ? m.getSender().getUserId() : null,
                m.getSender() != null ? m.getSender().getFullName() : "Khách",
                m.getContent(),
                m.getSentAt()
        ));
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/conversations/branch")
    @PreAuthorize("hasRole('BRANCH_MANAGER')")
    public ResponseEntity<List<Conversation>> getBranchOpenConversations(@RequestParam String branchId) {
        List<Conversation> list = conversationRepository.findByBranch_BranchIdAndStatusOrderByCreatedAtDesc(branchId, (byte)0);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/conversations/{convoId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> sendMessageViaRest(@PathVariable String convoId,
                                                                  @RequestBody Map<String, String> body,
                                                                  Authentication authentication) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Conversation convo = conversationRepository.findById(convoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        User sender = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Allow: conversation owner OR branch manager of this conversation's branch
        boolean allowed = sender.getUserId().equals(convo.getUser().getUserId());
        if (!allowed && convo.getBranch() != null) {
            String convoBranchId = convo.getBranch().getBranchId();
            allowed = userRoleRepository.findByUser(sender).stream()
                    .anyMatch(ur -> ur.getBranch() != null
                            && convoBranchId.equals(ur.getBranch().getBranchId())
                            && ur.getRole() != null
                            && ur.getRole().getRoleCode() == RoleCode.BRANCH_MANAGER);
        }
        if (!allowed) return ResponseEntity.status(403).build();

        Message msg = Message.builder()
                .conversation(convo)
                .sender(sender)
                .content(content)
                .sentAt(java.time.LocalDateTime.now())
                .build();
        msg = messageRepository.save(msg);

        // Broadcast to conversation topic
        Map<String, Object> out = new HashMap<>();
        out.put("messageId", msg.getMessageId());
        out.put("convoId", convo.getConvoId());
        out.put("senderUserId", sender.getUserId());
        out.put("senderName", sender.getFullName());
        out.put("content", msg.getContent());
        out.put("sentAt", msg.getSentAt());
        messagingTemplate.convertAndSend("/topic/conversations." + convoId, out);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        return ResponseEntity.ok(res);
    }
}


