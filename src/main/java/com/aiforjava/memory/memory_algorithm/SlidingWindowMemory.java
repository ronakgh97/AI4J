
package com.aiforjava.memory.memory_algorithm;

import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * SlidingWindowMemory is an implementation of the MemoryManager interface that maintains
 * a conversation history using a fixed-size sliding window. When the number of messages
 * exceeds the maximum allowed, the oldest message is automatically removed to make space
 * for new ones. This is useful for managing context in chatbots while limiting memory usage.
 * <p>
 * Note: For most use cases, {@link OptimizedSlidingWindowMemory} is recommended over this class
 * for new implementations due to its more efficient use of {@link java.util.Deque}.
 */
public class SlidingWindowMemory implements MemoryManager {
    // The maximum number of messages to retain in memory.
    private final int maxMessages;
    // A LinkedList is used to efficiently add and remove messages from both ends.
    private final LinkedList<Message> messages;

    /**
     * Constructs a new SlidingWindowMemory with a specified maximum number of messages.
     *
     * @param maxMessages The maximum number of messages to keep in the conversation history.
     *                    Must be a positive integer.
     * @throws IllegalArgumentException if maxMessages is not positive.
     */
    public SlidingWindowMemory(int maxMessages) {
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages must be positive");
        }
        this.maxMessages = maxMessages;
        this.messages = new LinkedList<>();
    }

    /**
     * Adds a new message to the conversation history.
     * If the number of messages already equals or exceeds `maxMessages`, the oldest message is removed
     * before the new one is added.
     *
     * @param message The Message object to be added. Must not be null.
     * @throws IllegalArgumentException if the message is null.
     */
    @Override
    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        // If the memory is full, remove the oldest message.
        if (messages.size() >= maxMessages) {
            messages.removeFirst();
        }
        // Add the new message to the end of the list.
        messages.add(message);
    }

    /**
     * Retrieves a copy of the list of all messages currently in the memory.
     *
     * @return A new ArrayList containing the messages in the current conversation history.
     *         Returning a copy prevents external modification of the internal message list.
     */
    @Override
    public List<Message> getMessagesList() {
        // Return a new ArrayList to prevent external modification of the internal LinkedList.
        return new ArrayList<>(messages);
    }

    /**
     * Clears all messages from the conversation history.
     */
    @Override
    public void clear() {
        messages.clear();
    }
}

