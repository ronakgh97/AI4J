package com.aiforjava.llm.Chat.LowLevel;

import com.aiforjava.exception.LLMParseException;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.client.LLMResponse;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.streams.StreamHandler;
import com.aiforjava.message.Message;
import com.aiforjava.message.files.ImagePart;
import com.aiforjava.message.files.TextPart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import com.aiforjava.memory.cache.LLMCacheManager;

/**
 * Provides low-level chat services for interacting with Large Language Models (LLMs).
 * This class offers methods to send chat completion requests, both with and without streaming,
 * and also provides raw access for full control over the JSON request payload.
 * It is designed for users who need direct control over the API requests.
 */
public class ChatServices_LowLevel {
    private final LLM_Client client;
    private final String modelName;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LLMCacheManager cacheManager;

    /**
     * Constructs a new ChatServices_LowLevel instance.
     *
     * @param client The LLM client responsible for sending HTTP requests to the LLM.
     * @param modelName The name of the LLM model to be used for chat completions (e.g., "gemma-3-4b-it").
     */
    public ChatServices_LowLevel(LLM_Client client, String modelName) {
        this(client, modelName, null);
    }

    /**
     * Constructs a new ChatServices_LowLevel instance with an optional cache manager.
     *
     * @param client The LLM client responsible for sending HTTP requests to the LLM.
     * @param modelName The name of the LLM model to be used for chat completions (e.g., "gemma-3-4b-it").
     * @param cacheManager An optional LLMCacheManager for caching LLM responses.
     */
    public ChatServices_LowLevel(LLM_Client client, String modelName, LLMCacheManager cacheManager) {
        this.client = client;
        this.modelName = modelName;
        this.cacheManager = cacheManager;
    }

    /**
     * Generates a chat completion response from the LLM without streaming.
     * This method constructs the JSON request based on the provided messages and model parameters.
     *
     * @param messages A list of Message objects representing the conversation history.
     * @param params ModelParams object containing parameters like temperature, max tokens, etc.
     * @return The generated response content from the LLM.
     * @throws LLMServiceException If any service-related error occurs during the request.
     */
    public LLMResponse generate(List<Message> messages, ModelParams params) throws LLMServiceException {
        try {
            String requestJson = buildRequest(messages, params, false);
            String response = client.sendRequest("v1/chat/completions", requestJson);
            return parseResponse(response);
        } catch (LLMServiceException e) {
            throw e;
        }
    }

    /**
     * Generates a chat completion response from the LLM, utilizing a cache if available.
     * This method constructs the JSON request based on the provided messages and model parameters.
     * If a cached response is found, it is returned directly; otherwise, an LLM call is made
     * and the response is stored in the cache.
     *
     * @param messages A list of Message objects representing the conversation history.
     * @param params ModelParams object containing parameters like temperature, max tokens, etc.
     * @return The generated response content from the LLM.
     * @throws LLMServiceException If any service-related error occurs during the request.
     */
    public LLMResponse generateWithCache(List<Message> messages, ModelParams params) throws LLMServiceException {
        String cacheKey = generateCacheKey(messages, params);
        if (cacheManager != null) {
            LLMResponse cachedResponse = cacheManager.get(cacheKey);
            if (cachedResponse != null) {
                return cachedResponse;
            }
        }

        LLMResponse llmResponse = generate(messages, params); // Use the existing generate method

        if (cacheManager != null) {
            cacheManager.put(cacheKey, llmResponse);
        }
        return llmResponse;
    }

    /**
     * Generates a unique cache key based on the messages and model parameters.
     * This key is used to store and retrieve responses from the cache.
     *
     * @param messages A list of Message objects.
     * @param params ModelParams object.
     * @return A string representing the cache key.
     */
    private String generateCacheKey(List<Message> messages, ModelParams params) {
        // A simple concatenation for demonstration. For production, consider a more robust hashing.
        StringBuilder keyBuilder = new StringBuilder();
        for (Message msg : messages) {
            keyBuilder.append(msg.getRole().name()).append(":");
            for (com.aiforjava.message.MessagePart part : msg.getContentParts()) {
                if (part instanceof TextPart) {
                    keyBuilder.append(((TextPart) part).getText());
                } else if (part instanceof ImagePart) {
                    // For image parts, use a hash of the image data or a unique identifier
                    keyBuilder.append("[IMAGE]"); // Placeholder for now
                }
            }
            keyBuilder.append("|");
        }
        keyBuilder.append("temp:").append(params.getTemperature());
        keyBuilder.append("maxTokens:").append(params.getMaxTokens());
        keyBuilder.append("topP:").append(params.getTopP());
        // Add other relevant parameters to the key
        return keyBuilder.toString();
    }

    /**
     * Generates a chat completion response from the LLM with streaming.
     * The response content is delivered in chunks via the provided StreamHandler.
     *
     * @param messages A list of Message objects representing the conversation history.
     * @param params ModelParams object containing parameters like temperature, max tokens, etc.
     * @param handler A StreamHandler to process the incoming stream of content.
     * @throws LLMServiceException If any error occurs during streaming generation.
     */
    public void generateStream(List<Message> messages, ModelParams params, StreamHandler handler) throws LLMServiceException, LLMParseException {
        String requestJson = buildRequest(messages, params, true);
        client.sendStreamRequest("v1/chat/completions", requestJson, handler);
    }

    /**
     * Sends a raw JSON request to a specified LLM endpoint and returns the raw JSON response.
     * This method provides the lowest level of control, allowing the user to craft the entire
     * request payload.
     *
     * @param endpoint The specific API endpoint to send the request to (e.g., "v1/chat/completions").
     * @param requestJson The complete JSON request payload as a string.
     * @return The raw JSON response from the LLM as a string.
     * @throws LLMServiceException If any error occurs during the raw request generation.
     */
    public String generateRaw(String endpoint, String requestJson) throws LLMServiceException {
        return client.sendRequest(endpoint, requestJson);
    }

    /**
     * Sends a raw JSON request to a specified LLM endpoint for streaming responses.
     * This method provides the lowest level of control for streaming, allowing the user
     * to craft the entire request payload.
     *
     * @param endpoint The specific API endpoint to send the request to (e.g., "v1/chat/completions").
     * @param requestJson The complete JSON request payload as a string.
     * @param handler A StreamHandler to process the incoming stream of content.
     * @throws LLMServiceException If any error occurs during the raw streaming generation.
     */
    public void generateStreamRaw(String endpoint, String requestJson, StreamHandler handler) throws LLMServiceException, LLMParseException {
        client.sendStreamRequest(endpoint, requestJson, handler);
    }

    /**
     * Builds the JSON request payload for chat completion based on messages and model parameters.
     * This internal helper method ensures the request conforms to the LLM's API specification.
     *
     * @param messages A list of Message objects.
     * @param params ModelParams object.
     * @param stream A boolean indicating whether the request is for streaming or not.
     * @return The JSON request payload as a string.
     */
    private String buildRequest(List<Message> messages, ModelParams params, boolean stream) {
        ObjectNode request = mapper.createObjectNode();
        request.put("model", modelName);
        request.put("temperature", params.getTemperature());
        request.put("stream", stream);

        ArrayNode messagesNode = request.putArray("messages");
        for (Message msg : messages) {
            ObjectNode messageContentNode = mapper.createObjectNode();
            messageContentNode.put("role", msg.getRole().name().toLowerCase());

            ArrayNode contentArray = messageContentNode.putArray("content");
            for (com.aiforjava.message.MessagePart part : msg.getContentParts()) {
                if (part instanceof TextPart) {
                    contentArray.add(mapper.createObjectNode()
                            .put("type", "text")
                            .put("text", ((TextPart) part).getText()));
                } else if (part instanceof ImagePart) {
                    contentArray.add(mapper.valueToTree(part)); // Jackson will handle ImagePart's JsonCreator
                }
            }
            messagesNode.add(messageContentNode);
        }

        return request.toString();
    }

    /**
     * Parses the raw JSON response from the LLM to extract the generated content.
     * This method assumes a standard LLM response structure (e.g., OpenAI-compatible).
     *
     * @param response The raw JSON response string from the LLM.
     * @return The extracted content of the LLM's response.
     * @throws LLMParseException if the response format is invalid or content cannot be extracted.
     */
    private LLMResponse parseResponse(String response) throws LLMParseException {
        try {
            JsonNode root = mapper.readTree(response);

            if (!root.has("choices") || !root.get("choices").isArray() || root.get("choices").isEmpty())
                throw new LLMParseException("Invalid response: missing or empty choices array");

            JsonNode firstChoice = root.path("choices").get(0);

            if (!firstChoice.has("message") || !firstChoice.path("message").has("content"))
                throw new LLMParseException("Invalid response: missing message content");

            String content = firstChoice.path("message").path("content").asText();

            Integer totalTokens = null;
            if (root.has("usage")) {
                JsonNode usageNode = root.path("usage");
                if (usageNode.has("total_tokens")) {
                    totalTokens = usageNode.path("total_tokens").asInt();
                }
            }

            return new LLMResponse(content, totalTokens);
        } catch (JsonProcessingException e) {
            throw new LLMParseException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }
}
