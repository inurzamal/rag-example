package com.nur.controller;

import com.nur.dto.ChatRequest;
import com.nur.dto.ChatResponse;
import com.nur.services.RagChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rag")
@Tag(name = "RAG Chat", description = "Chat with documents using RAG + memory")
public class RagController {

    private final RagChatService ragChatService;

    @Operation(summary = "Chat with your documents",
            description = "Sends a message and gets an answer grounded in ingested PDFs. " +
                    "Use the same conversationId to maintain chat history.")
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = ragChatService.chat(request.conversationId(), request.message());
        return new ChatResponse(request.conversationId(), answer);
    }
}