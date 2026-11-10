package com.mediqai.ai.controller;

import com.mediqai.ai.dto.request.ChatRequest;
import com.mediqai.ai.dto.response.ChatResponse;
import com.mediqai.ai.dto.response.ConversationResponse;
import com.mediqai.ai.dto.response.MessageResponse;
import com.mediqai.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        ChatResponse response =
                aiService.chat(email, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<ConversationResponse> conversations =
                aiService.getConversations(email);

        return ResponseEntity.ok(conversations);
    }


    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            Authentication authentication,
            @PathVariable Long conversationId
    ) {

        String email = authentication.getName();

        List<MessageResponse> messages =
                aiService.getMessages(
                        email,
                        conversationId
                );

        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<String> deleteConversation(
            Authentication authentication,
            @PathVariable Long conversationId
    ) {

        String email = authentication.getName();

        aiService.deleteConversation(
                email,
                conversationId
        );

        return ResponseEntity.ok(
                "Xóa cuộc hội thoại thành công"
        );
    }
}