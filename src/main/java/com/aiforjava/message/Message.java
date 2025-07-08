package com.aiforjava.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Represents a single message in a conversation with a Large Language Model (LLM).
 * Each message has a role (e.g., SYSTEM, USER, ASSISTANT), content, and a timestamp.
 * This class is designed to be immutable once created.
 */
public class Message {
    private final MessageRole role;
    private final String content;
    private final Instant time;

    /**
     * Constructor used by Jackson for deserialization from JSON.
     * It allows reconstructing a Message object from its JSON representation.
     *
     * @param role The role of the message sender.
     * @param content The content of the message.
     * @param time The timestamp of the message.
     * @throws IllegalArgumentException if role, content, or time is null.
     */
    @JsonCreator
    public Message(
            @JsonProperty("role") MessageRole role,
            @JsonProperty("content") String content,
            @JsonProperty("time") Instant time) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null");
        }
        this.role = role;
        this.content = content;
        this.time = time;
    }

    /**
     * Convenience constructor for creating a new message with the current timestamp.
     * The timestamp is automatically set to `Instant.now()`.
     *
     * @param role The role of the message sender.
     * @param content The content of the message.
     */
    public Message(MessageRole role, String content) {
        this(role, content, Instant.now());
    }

    /**
     * Returns the role of the message sender.
     * @return The MessageRole of this message.
     */
    public MessageRole getRole() {
        return role;
    }

    /**
     * Returns the text content of the message.
     * @return The String content of this message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the timestamp when this message was created.
     * @return The Instant timestamp of this message.
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Returns a string representation of the message, including its timestamp, role, and content.
     * @return A formatted string representing the message.
     */
    @Override
    public String toString() {
        return String.format("[%s][%s] %s",
                time.toString(),
                role.name(),
                content);
    }
}
