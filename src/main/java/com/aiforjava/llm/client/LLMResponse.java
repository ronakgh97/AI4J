package com.aiforjava.llm.client;

public class LLMResponse {
    private final String content;
    private final Integer totalTokens; // Nullable if token count is not available

    public LLMResponse(String content, Integer totalTokens) {
        this.content = content;
        this.totalTokens = totalTokens;
    }

    public String getContent() {
        return content;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }
}
