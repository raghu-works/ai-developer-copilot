package com.copilot.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.copilot.backend.entity.ChatSession;
import com.copilot.backend.repository.ChatSessionRepository;
import com.copilot.backend.dto.CodeRequest;
import com.copilot.backend.service.AIService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    private final AIService aiService;
    private final ChatSessionRepository chatSessionRepository;

    public AIController(
            AIService aiService,
            ChatSessionRepository chatSessionRepository) {

        this.aiService = aiService;
        this.chatSessionRepository = chatSessionRepository;
    }

    // Create a new chat session
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(
            @RequestBody ChatSession session) {

        ChatSession savedSession =
                chatSessionRepository.save(session);

        return ResponseEntity.ok(savedSession);
    }

    // Send code to AI
    @PostMapping("/code")
    public ResponseEntity<String> processCode(
            @RequestBody CodeRequest request) {

        String response =
                aiService.processCode(request);

        return ResponseEntity.ok(response);
    }
}
