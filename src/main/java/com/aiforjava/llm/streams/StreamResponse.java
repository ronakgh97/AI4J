package com.aiforjava.llm.streams;

public class StreamResponse {

    private final String content;
    private final String reasoningContent;

    public StreamResponse(String content, String reasoningContent) {
        this.content = content;
        this.reasoningContent = reasoningContent;
    }

    public String getContent() {
        return content;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }
}