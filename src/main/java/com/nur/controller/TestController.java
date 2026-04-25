package com.nur.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final EmbeddingModel embeddingModel;

    public TestController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/test-embed")
    public float[] test() {
        return embeddingModel.embed("hello");
    }
}
