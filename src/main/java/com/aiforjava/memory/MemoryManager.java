
package com.aiforjava.memory;

import com.aiforjava.message.Message;

import java.util.List;

/**
 * The MemoryManager interface defines the contract for managing conversation history
 * within a chatbot or LLM application. Implementations of this interface are responsible
 * for storing, retrieving, and clearing messages to maintain conversational context.
 * This abstraction allows for different memory strategies (e.g., in-memory, file-based)
 * to be used interchangeably.
 */
public interface MemoryManager {

    /**
     * Adds a new message to the conversation history.
     *
     * @param message The Message object to be added.
     */
    void addMessage(Message message);

    /**
     * Retrieves the current list of messages from the conversation history.
     *
     * @return A List of Message objects representing the current conversation.
     */
    List<Message> getMessagesList();

    /**
     * Clears all messages from the conversation history, effectively starting a new conversation.
     */
    void clear();
}

