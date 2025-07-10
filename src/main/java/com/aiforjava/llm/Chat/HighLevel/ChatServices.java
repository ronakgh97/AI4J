package com.aiforjava.llm.Chat.HighLevel;

import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.exception.LLMParseException;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.client.LLMResponse;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.streams.StreamHandler;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.message.files.ImagePart;
import com.aiforjava.message.files.TextPart;
import com.aiforjava.message.MessagePart;
import com.aiforjava.util.ImageEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    private final PromptTemplate imageDescriptionPromptTemplate; // New prompt template for image descriptions

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
        // Initialize the image description prompt template
        this.imageDescriptionPromptTemplate = new PromptTemplate("Describe the image concisely.", "{image_description_request}");
        initialize();
    }

    /**
     * Initializes the chat service by clearing the memory and setting the system prompt.
     */
    private void initialize() {
        memory.clear();
        memory.addMessage(new Message(MessageRole.SYSTEM, promptTemplate.getSystemPrompt()));
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
        memory.addMessage(new Message(MessageRole.USER, promptTemplate.formatUserMessage(userMessage)));
        LLMResponse llmResponse = llm.generate(memory.getMessagesList(), params);
        Message assistantMessage = new Message(MessageRole.ASSISTANT, llmResponse.getContent());
        assistantMessage.setTokenCount(llmResponse.getTotalTokens());
        memory.addMessage(assistantMessage);
        return llmResponse.getContent();
    }

    /**
     * Sends a user message along with an image to the LLM and returns the response.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @return The LLM's response.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public String chat(String userMessage, File imageFile) throws LLMServiceException, IOException {
        return chat(userMessage, imageFile, defaultParams);
    }

    /**
     * Sends a user message along with an image to the LLM with custom model parameters and returns the response.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @param params The model parameters to use for this request.
     * @return The LLM's response.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public String chat(String userMessage, File imageFile, ModelParams params) throws LLMServiceException, IOException {
        String base64Image = ImageEncoder.encodeImageToBase64(imageFile);
        List<MessagePart> contentParts = Arrays.asList(
                new TextPart(promptTemplate.formatUserMessage(userMessage)),
                new ImagePart(base64Image)
        );
        memory.addMessage(new Message(MessageRole.USER, contentParts));

        // Generate and store image description for memory persistence
        try {
            List<MessagePart> imageDescriptionContent = Arrays.asList(
                    new TextPart(imageDescriptionPromptTemplate.getSystemPrompt()),
                    new ImagePart(base64Image)
            );
            // Use a minimal ModelParams for description generation
            ModelParams descriptionParams = new ModelParams.Builder()
                    .setTemperature(0.0)
                    .setMaxTokens(50) // Keep description concise
                    .build();
            LLMResponse imageDescriptionResponse = llm.generate(Arrays.asList(new Message(MessageRole.USER, imageDescriptionContent)), descriptionParams);
            memory.addMessage(new Message(MessageRole.SYSTEM, "Image Description: " + imageDescriptionResponse.getContent()));
        } catch (LLMParseException e) {
            // Log the error but don't fail the main chat operation
            System.err.println("Warning: Failed to generate image description: " + e.getMessage());
        }

        LLMResponse llmResponse = llm.generate(memory.getMessagesList(), params);
        Message assistantMessage = new Message(MessageRole.ASSISTANT, llmResponse.getContent());
        assistantMessage.setTokenCount(llmResponse.getTotalTokens());
        memory.addMessage(assistantMessage);
        return llmResponse.getContent();
    }

    /**
     * Sends a user message to the LLM and returns the response along with token information.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @return An LLMResponse object containing the LLM's response and token count.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public LLMResponse chatAndGetTokens(String userMessage) throws LLMServiceException {
        return chatAndGetTokens(userMessage, defaultParams);
    }

    /**
     * Sends a user message to the LLM with custom model parameters and returns the response along with token information.
     *
     * @param userMessage The user's message.
     * @param params The model parameters to use for this request.
     * @return An LLMResponse object containing the LLM's response and token count.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public LLMResponse chatAndGetTokens(String userMessage, ModelParams params) throws LLMServiceException {
        memory.addMessage(new Message(MessageRole.USER, promptTemplate.formatUserMessage(userMessage)));
        LLMResponse llmResponse = llm.generate(memory.getMessagesList(), params);
        Message assistantMessage = new Message(MessageRole.ASSISTANT, llmResponse.getContent());
        assistantMessage.setTokenCount(llmResponse.getTotalTokens());
        memory.addMessage(assistantMessage);
        return llmResponse;
    }

    /**
     * Sends a user message along with an image to the LLM and returns the response along with token information.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @return An LLMResponse object containing the LLM's response and token count.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public LLMResponse chatAndGetTokens(String userMessage, File imageFile) throws LLMServiceException, IOException {
        return chatAndGetTokens(userMessage, imageFile, defaultParams);
    }

    /**
     * Sends a user message along with an image to the LLM with custom model parameters and returns the response along with token information.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @param params The model parameters to use for this request.
     * @return An LLMResponse object containing the LLM's response and token count.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public LLMResponse chatAndGetTokens(String userMessage, File imageFile, ModelParams params) throws LLMServiceException, IOException {
        String base64Image = ImageEncoder.encodeImageToBase64(imageFile);
        List<MessagePart> contentParts = Arrays.asList(
                new TextPart(promptTemplate.formatUserMessage(userMessage)),
                new ImagePart(base64Image)
        );
        memory.addMessage(new Message(MessageRole.USER, contentParts));

        // Generate and store image description for memory persistence
        try {
            List<MessagePart> imageDescriptionContent = Arrays.asList(
                    new TextPart(imageDescriptionPromptTemplate.getSystemPrompt()),
                    new ImagePart(base64Image)
            );
            // Use a minimal ModelParams for description generation
            ModelParams descriptionParams = new ModelParams.Builder()
                    .setTemperature(0.0)
                    .setMaxTokens(50) // Keep description concise
                    .build();
            LLMResponse imageDescriptionResponse = llm.generate(Arrays.asList(new Message(MessageRole.USER, imageDescriptionContent)), descriptionParams);
            memory.addMessage(new Message(MessageRole.SYSTEM, "Image Description: " + imageDescriptionResponse.getContent()));
        } catch (LLMParseException e) {
            // Log the error but don't fail the main chat operation
            System.err.println("Warning: Failed to generate image description: " + e.getMessage());
        }

        LLMResponse llmResponse = llm.generate(memory.getMessagesList(), params);
        Message assistantMessage = new Message(MessageRole.ASSISTANT, llmResponse.getContent());
        assistantMessage.setTokenCount(llmResponse.getTotalTokens());
        memory.addMessage(assistantMessage);
        return llmResponse;
    }

    /**
     * Sends a user message to the LLM and streams the response.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @param handler The stream handler to process the response chunks.
     * @throws LLMServiceException If an error occurs during the chat completion.
     */
    public void chatStream(String userMessage, StreamHandler handler) throws LLMServiceException, LLMParseException {
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
    public void chatStream(String userMessage, ModelParams params, StreamHandler handler) throws LLMServiceException, LLMParseException {
        boolean noThink = userMessage.endsWith("/no_think");
        if (noThink) {
            userMessage = userMessage.substring(0, userMessage.length() - "/no_think".length());
        }

        memory.addMessage(new Message(MessageRole.USER, promptTemplate.formatUserMessage(userMessage)));

        List<Message> messagesToSend = new java.util.ArrayList<>(memory.getMessagesList());
        if (noThink) {
            Message lastUserMessage = messagesToSend.get(messagesToSend.size() - 1);
            MessagePart firstPart = lastUserMessage.getContentParts().get(0);
            if (firstPart instanceof TextPart) {
                TextPart textPart = (TextPart) firstPart;
                Message modifiedMessage = new Message(lastUserMessage.getRole(), Arrays.asList(new TextPart(textPart.getText() + "/no_think")));
                messagesToSend.set(messagesToSend.size() - 1, modifiedMessage);
            }
        }

        StringBuilder response = new StringBuilder();
        llm.generateStream(messagesToSend, params, content -> {
            response.append(content);
            handler.onStream(content);
        });
        Message assistantMessage = new Message(MessageRole.ASSISTANT, response.toString());
        // For streaming, we estimate tokens as actual token count is not directly available from stream
        assistantMessage.setTokenCount(com.aiforjava.util.TokenCalculator.estimateTokens(assistantMessage));
        memory.addMessage(assistantMessage);
    }

    /**
     * Sends a user message along with an image to the LLM and streams the response.
     * This method uses the default model parameters.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @param handler The stream handler to process the response chunks.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public void chatStream(String userMessage, File imageFile, StreamHandler handler) throws LLMServiceException, IOException, LLMParseException {
        chatStream(userMessage, imageFile, defaultParams, handler);
    }

    /**
     * Sends a user message along with an image to the LLM with custom model parameters and streams the response.
     *
     * @param userMessage The user's message.
     * @param imageFile The image file to send.
     * @param params The model parameters to use for this request.
     * @param handler The stream handler to process the response chunks.
     * @throws LLMServiceException If an error occurs during the chat completion.
     * @throws IOException If an I/O error occurs while reading the image file.
     */
    public void chatStream(String userMessage, File imageFile, ModelParams params, StreamHandler handler) throws LLMServiceException, IOException, LLMParseException {
        boolean noThink = userMessage.endsWith("/no_think");
        if (noThink) {
            userMessage = userMessage.substring(0, userMessage.length() - "/no_think".length());
        }

        String base64Image = ImageEncoder.encodeImageToBase64(imageFile);
        List<MessagePart> contentParts = Arrays.asList(
                new TextPart(promptTemplate.formatUserMessage(userMessage)),
                new ImagePart(base64Image)
        );
        memory.addMessage(new Message(MessageRole.USER, contentParts));

        // Generate and store image description for memory persistence
        try {
            List<MessagePart> imageDescriptionContent = Arrays.asList(
                    new TextPart(imageDescriptionPromptTemplate.getSystemPrompt()),
                    new ImagePart(base64Image)
            );
            // Use a minimal ModelParams for description generation
            ModelParams descriptionParams = new ModelParams.Builder()
                    .setTemperature(0.0)
                    .setMaxTokens(50) // Keep description concise
                    .build();
            LLMResponse imageDescriptionResponse = llm.generate(Arrays.asList(new Message(MessageRole.USER, imageDescriptionContent)), descriptionParams);
            memory.addMessage(new Message(MessageRole.SYSTEM, "Image Description: " + imageDescriptionResponse.getContent()));
        } catch (LLMParseException e) {
            // Log the error but don't fail the main chat operation
            System.err.println("Warning: Failed to generate image description: " + e.getMessage());
        }

        List<Message> messagesToSend = new java.util.ArrayList<>(memory.getMessagesList());
        if (noThink) {
            Message lastUserMessage = messagesToSend.get(messagesToSend.size() - 1);
            MessagePart firstPart = lastUserMessage.getContentParts().get(0);
            if (firstPart instanceof TextPart) {
                TextPart textPart = (TextPart) firstPart;
                List<MessagePart> newParts = new java.util.ArrayList<>(lastUserMessage.getContentParts());
                newParts.set(0, new TextPart(textPart.getText() + "/no_think"));
                Message modifiedMessage = new Message(lastUserMessage.getRole(), newParts);
                messagesToSend.set(messagesToSend.size() - 1, modifiedMessage);
            }
        }

        StringBuilder response = new StringBuilder();
        llm.generateStream(messagesToSend, params, content -> {
            response.append(content);
            handler.onStream(content);
        });
        Message assistantMessage = new Message(MessageRole.ASSISTANT, response.toString());
        // For streaming, we estimate tokens as actual token count is not directly available from stream
        assistantMessage.setTokenCount(com.aiforjava.util.TokenCalculator.estimateTokens(assistantMessage));
        memory.addMessage(assistantMessage);
    }

    /**
     * Resets the conversation history to its initial state.
     */
    public void reset() {
        initialize();
    }
}