package com.aiforjava.message;

import com.aiforjava.message.files.ImagePart;
import com.aiforjava.message.files.TextPart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a single message in a conversation with a Large Language Model (LLM).
 * Each message has a role (e.g., SYSTEM, USER, ASSISTANT), content (which can be multimodal),
 * and a timestamp. This class is designed to be immutable once created.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON output
public class Message {
    private final MessageRole role;
    private final List<MessagePart> contentParts;
    private final Instant time;
    private Integer tokenCount; // Optional: to store actual token count from LLM response

    /**
     * Constructor used by Jackson for deserialization from JSON.
     * It allows reconstructing a Message object from its JSON representation.
     *
     * @param role The role of the message sender.
     * @param contentParts The content of the message as a list of MessagePart objects.
     * @param time The timestamp of the message.
     * @param tokenCount Optional: The actual token count of the message as reported by the LLM.
     * @throws IllegalArgumentException if role, contentParts, or time is null.
     */
    @JsonCreator
    public Message(
            @JsonProperty("role") MessageRole role,
            @JsonProperty("content") List<MessagePart> contentParts,
            @JsonProperty("time") Instant time,
            @JsonProperty("tokenCount") Integer tokenCount) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (contentParts == null || contentParts.isEmpty()) {
            throw new IllegalArgumentException("Content parts cannot be null or empty");
        }
        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
        this.role = role;
        this.contentParts = Collections.unmodifiableList(new ArrayList<>(contentParts));
        this.time = time;
        this.tokenCount = tokenCount;
    }

    /**
     * Convenience constructor for creating a new message with the current timestamp.
     * The timestamp is automatically set to `Instant.now()`.
     *
     * @param role The role of the message sender.
     * @param content The content of the message.
     */
    public Message(MessageRole role, String content) {
        this(role, Collections.singletonList(new TextPart(content)), Instant.now(), null);
    }

    /**
     * Convenience constructor for creating a new multimodal message with the current timestamp.
     * The timestamp is automatically set to `Instant.now()`.
     *
     * @param role The role of the message sender.
     * @param contentParts The content of the message as a list of MessagePart objects.
     */
    public Message(MessageRole role, List<MessagePart> contentParts) {
        this(role, contentParts, Instant.now(), null);
    }

    /**
     * Returns the role of the message sender.
     * @return The MessageRole of this message.
     */
    public MessageRole getRole() {
        return role;
    }

    /**
     * Returns the content parts of the message.
     * @return The list of MessagePart objects.
     */
    public List<MessagePart> getContentParts() {
        return contentParts;
    }

    /**
     * Returns the timestamp when this message was created.
     * @return The Instant timestamp of this message.
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Returns the estimated or actual token count of this message.
     * @return The token count, or null if not set.
     */
    public Integer getTokenCount() {
        return tokenCount;
    }

    /**
     * Sets the token count for this message.
     * This is typically used to update the message with the actual token count
     * received from the LLM API response.
     * @param tokenCount The actual token count.
     */
    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    /**
     * Returns a string representation of the message, including its timestamp, role, and content.
     * @return A formatted string representing the message.
     */
    @Override
    public String toString() {
        StringBuilder contentBuilder = new StringBuilder();
        for (MessagePart part : contentParts) {
            if (part instanceof TextPart) {
                contentBuilder.append(((TextPart) part).getText());
            } else if (part instanceof ImagePart) {
                contentBuilder.append("[Image]"); // Or a more detailed representation
            }
        }
        return String.format("[%s][%s] %s",
                time.toString(),
                role.name(),
                contentBuilder.toString());
    }
}
