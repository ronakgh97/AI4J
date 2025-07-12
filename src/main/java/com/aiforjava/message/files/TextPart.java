package com.aiforjava.message.files;

import com.aiforjava.message.MessagePart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a text part of a multimodal message.
 */
public class TextPart implements MessagePart {
    private final String text;

    @JsonCreator
    public TextPart(@JsonProperty("text") String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text content cannot be null or empty");
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
