package com.aiforjava.memory.memory_algorithm;

import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An optimized implementation of {@link MemoryManager} that uses a {@link Deque}
 * (specifically {@link ArrayDeque}) to efficiently manage a sliding window of messages.
 * This provides O(1) time complexity for adding and removing messages from both ends,
 * making it more performant than a traditional {@link java.util.ArrayList} for this use case.
 */
public class OptimizedSlidingWindowMemory implements MemoryManager {

    private final Deque<Message> messages;
    private final int capacity;

    /**
     * Constructs a new OptimizedSlidingWindowMemory with a specified capacity.
     *
     * @param capacity The maximum number of messages to retain in memory. Must be positive.
     * @throws IllegalArgumentException if the capacity is not positive.
     */
    public OptimizedSlidingWindowMemory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.messages = new ArrayDeque<>(capacity);
    }

    @Override
    public void addMessage(Message message) {
        if (messages.size() == capacity) {
            messages.removeFirst(); // Remove the oldest message
        }
        messages.addLast(message); // Add the new message to the end
    }

    @Override
    public List<Message> getMessagesList() {
        // Return an immutable copy to prevent external modification
        return messages.stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void clear() {
        messages.clear();
    }
}