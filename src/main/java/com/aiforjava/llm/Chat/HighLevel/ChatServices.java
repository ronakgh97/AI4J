
package com.aiforjava.llm.Chat.HighLevel;

import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.llm.StreamHandler;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;
import com.aiforjava.llm.Prompt.PromptTemplate;

/**
 * Provides a high-level abstraction for chat interactions with an LLM.
 * This class manages conversational state, including message history and system prompts,
 * making it easier to build stateful chatbots. It uses a {@link MemoryManager} to handle
 * the conversation history and a {@link ChatServices_LowLevel} instance to communicate with the LLM.
 */
public class ChatServices {
    private final ChatServices_LowLevel llm;
    private final MemoryManager memory;
    private final ModelParams defaultParams;
    private final PromptTemplate promptTemplate;

    /**
     * Constructs a new ChatServices instance.
     *
     * @param llm The low-level chat service for communicating with the LLM.
     * @param memory The memory manager for storing and retrieving conversation history.
     * @param defaultParams The default model parameters to use for chat completions.
     * @param promptTemplate The prompt template to use for formatting messages.
     */
    public ChatServices(
            ChatServices_LowLevel llm,
            MemoryManager memory,
            ModelParams defaultParams,
            PromptTemplate promptTemplate
    ) {
        this.llm = llm;
        this.memory = memory;
        this.defaultParams = defaultParams;
        this.promptTemplate = promptTemplate;
        initialize();
    }

    /**
     * Initializes the chat service by clearing the memory and setting the system prompt.
     */
    private void initialize() {
        memory.clear();
        memory.addMessage(new Message(MessageRole.SYSTEM, promptTemplate.build()));
    }

    /**
     * Sends a user message to the LLM and returns the response.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @return The LLM's response.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public String chat(String userMessage) throws LLMServiceException {
        return chat(userMessage, defaultParams);
    }

    /**
     * Sends a user message to the LLM with custom model parameters and returns the response.
     *
     * @param userMessage The user's message.
     * @param params The model parameters to use for this request.
     * @return The LLM's response.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public String chat(String userMessage, ModelParams params) throws LLMServiceException {
        memory.addMessage(new Message(MessageRole.USER, promptTemplate.set("user_message", userMessage).build()));
        String response = llm.generate(memory.getMessagesList(), params);
        memory.addMessage(new Message(MessageRole.ASSISTANT, response));
        return response;
    }

    /**
     * Sends a user message to the LLM and streams the response.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @param handler The stream handler to process the response chunks.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public void chatStream(String userMessage, StreamHandler handler) throws LLMServiceException {
        chatStream(userMessage, defaultParams, handler);
    }

    /**
     * Sends a user message to the LLM with custom model parameters and streams the response.
     *
     * @param userMessage The user's message.
     * @param params The model parameters to use for this request.
     * @param handler The stream handler to process the response chunks.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public void chatStream(String userMessage, ModelParams params, StreamHandler handler) throws LLMServiceException {
        memory.addMessage(new Message(MessageRole.USER, promptTemplate.set("user_message", userMessage).build()));
        StringBuilder response = new StringBuilder();
        llm.generateStream(memory.getMessagesList(), params, content -> {
            response.append(content);
            handler.onStream(content);
        });
        memory.addMessage(new Message(MessageRole.ASSISTANT, response.toString()));
    }

    /**
     * Resets the conversation history to its initial state.
     */
    public void reset() {
        initialize();
    }
}

