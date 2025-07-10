package com.aiforjava.memory.memory_algorithm;

import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.aiforjava.util.TokenCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of MemoryManager that maintains a conversation history
 * based on a maximum token limit. When adding new messages, it removes older
 * messages from the history if the total token count exceeds the limit.
 * It prioritizes using actual token counts from Message objects if available,
 * otherwise, it falls back to TokenCalculator for estimation.
 */
public class TokenCountingMemory implements MemoryManager {

    private final Deque<Message> messages;
    private final int maxTokens;
    private int currentTokens;

    /**
     * Constructs a new TokenCountingMemory with a specified maximum token limit.
     *
     * @param maxTokens The maximum number of tokens to retain in memory.
     * @throws IllegalArgumentException if maxTokens is less than or equal to 0.
     */
    public TokenCountingMemory(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be greater than 0.");
        }
        this.maxTokens = maxTokens;
        this.messages = new LinkedList<>();
        this.currentTokens = 0;
    }

    @Override
    public void addMessage(Message message) {
        messages.addLast(message);
        currentTokens += getTokenCount(message);
        evictOldMessages();
    }

    @Override
    public List<Message> getMessagesList() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    @Override
    public void clear() {
        messages.clear();
        currentTokens = 0;
    }

    /**
     * Evicts old messages from the beginning of the conversation history
     * until the total token count is within the specified maximum limit.
     */
    private void evictOldMessages() {
        while (currentTokens > maxTokens && !messages.isEmpty()) {
            Message removedMessage = messages.removeFirst();
            currentTokens -= getTokenCount(removedMessage);
        }
    }

    /**
     * Gets the token count for a message, prioritizing the actual token count
     * from the Message object, and falling back to estimation if not available.
     *
     * @param message The message to get the token count for.
     * @return The token count.
     */
    private int getTokenCount(Message message) {
        if (message.getTokenCount() != null) {
            return message.getTokenCount();
        } else {
            // Fallback to estimation if actual token count is not set
            return TokenCalculator.estimateTokens(message);
        }
    }

    /**
     * Returns the current total number of tokens in memory.
     * @return The current token count.
     */
    public int getCurrentTokens() {
        return currentTokens;
    }

    /**
     * Returns the maximum allowed tokens for this memory instance.
     * @return The maximum token limit.
     */
    public int getMaxTokens() {
        return maxTokens;
    }
}
