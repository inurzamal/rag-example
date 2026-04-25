package com.nur.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class ChatAIConfiguration {

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(50)
                .build();
    }

//    @Bean
//    @Primary
//    public ChatClient geminiChatClient(GoogleGenAiChatModel googleGenAiChatModel, ChatMemory chatMemory) {
//
//        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
//
//        Advisor memoryAdvisor = MessageChatMemoryAdvisor
//                .builder(chatMemory)
//                .build();
//
//        return ChatClient.builder(googleGenAiChatModel)
//                .defaultAdvisors(List.of(memoryAdvisor, loggerAdvisor))
//                .build();
//    }

    @Bean
    @Primary
    public ChatClient geminiChatClient(GoogleGenAiChatModel googleGenAiChatModel) {
        return ChatClient.builder(googleGenAiChatModel)
                .defaultSystem("""
                    You are a helpful assistant for TechNova Solutions.
                    
                    You have two sources of information — use BOTH:
                    
                    1. DOCUMENT CONTEXT: Chunks from company documents injected into this prompt.
                       Use these to answer questions about TechNova products, HR policies, etc.
                    
                    2. CONVERSATION HISTORY: Previous messages in this conversation.
                       Use these to answer personal questions like the user's name, 
                       or anything the user mentioned earlier in the chat.
                    
                    Rules:
                    - If the answer is in document context → answer from documents.
                    - If the answer is in conversation history → answer from history.
                    - If the answer is in neither → say "I don't have that information."
                    - Never ignore conversation history when answering personal questions.
                    """)
                .build();
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(GoogleGenAiTextEmbeddingModel model) {
        return model;
    }

//    @Bean
//    @Primary
//    public EmbeddingModel embeddingModel(OpenAiEmbeddingModel model) {
//        return model;
//    }

}
