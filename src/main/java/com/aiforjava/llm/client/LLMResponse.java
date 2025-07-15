package com.aiforjava.llm.client;

public class LLMResponse {
    private final String content;
    private final String reasoningContent; // Nullable
    private final Integer totalTokens; // Nullable if token count is not available

    public LLMResponse(String content, String reasoningContent, Integer totalTokens) {
        this.content = content;
        this.reasoningContent = reasoningContent;
        this.totalTokens = totalTokens;
    }

    public String getContent() {
        return content;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }
}
