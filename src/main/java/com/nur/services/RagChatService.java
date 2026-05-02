package com.nur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    // Inject by @Primary — no need to use field name "geminiChatClient"
    public RagChatService(ChatClient chatClient, VectorStore vectorStore, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    public String chat(String conversationId, String userMessage) {
        log.debug("Chat request — conversationId: {}, message: {}", conversationId, userMessage);

        return chatClient.prompt()
                .advisors(
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(
                                        VectorStoreDocumentRetriever.builder()
                                                .vectorStore(vectorStore)
                                                .similarityThreshold(0.5)
                                                .topK(5)
                                                .build()
                                )
                                .build(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(conversationId)
                                .build()
                )
                .user(userMessage)
                .call()
                .content();
    }
}