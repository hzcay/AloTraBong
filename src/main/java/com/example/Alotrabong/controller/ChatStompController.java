package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.Conversation;
import com.example.Alotrabong.entity.Message;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.RoleCode;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.ConversationRepository;
import com.example.Alotrabong.repository.MessageRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatStompController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public static class ChatPayload {
        public String content;
        public String mediaUrl;
    }

    public static class ChatOut {
        public String messageId;
        public String convoId;
        public String senderUserId;
        public String senderName;
        public String content;
        public String mediaUrl;
        public LocalDateTime sentAt;
    }

    @MessageMapping("/chat/send/{convoId}")
    public void sendMessage(@DestinationVariable String convoId, @Payload ChatPayload payload, Principal principal) {
        if (principal == null) {
            return;
        }

        Conversation conversation = conversationRepository.findById(convoId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Authorization: user is conversation owner OR belongs to branch
        String senderEmail = principal.getName();
        User sender = userRepository.findByEmail(senderEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean allowed = sender.getUserId().equals(conversation.getUser().getUserId());
        if (!allowed && conversation.getBranch() != null) {
            String convoBranchId = conversation.getBranch().getBranchId();
            // Only branch managers of the conversation's branch can send
            allowed = userRoleRepository.findByUser(sender).stream()
                    .anyMatch(ur -> ur.getBranch() != null
                            && convoBranchId.equals(ur.getBranch().getBranchId())
                            && ur.getRole() != null
                            && ur.getRole().getRoleCode() == RoleCode.BRANCH_MANAGER);
        }
        if (!allowed) {
            log.warn("User {} not allowed to send message to convo {}", sender.getEmail(), convoId);
            return;
        }

        Message msg = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(payload != null ? payload.content : null)
                .mediaUrl(payload != null ? payload.mediaUrl : null)
                .sentAt(LocalDateTime.now())
                .build();
        msg = messageRepository.save(msg);

        ChatOut out = new ChatOut();
        out.messageId = msg.getMessageId();
        out.convoId = conversation.getConvoId();
        out.senderUserId = sender.getUserId();
        out.senderName = sender.getFullName();
        out.content = msg.getContent();
        out.mediaUrl = msg.getMediaUrl();
        out.sentAt = msg.getSentAt();

        messagingTemplate.convertAndSend("/topic/conversations." + convoId, out);
    }
}


