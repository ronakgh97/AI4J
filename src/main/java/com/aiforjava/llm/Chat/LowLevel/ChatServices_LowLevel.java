package com.aiforjava.llm.Chat.LowLevel;

import com.aiforjava.exception.LLMParseException;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.*;
import com.aiforjava.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

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

    /**
     * Constructs a new ChatServices_LowLevel instance.
     *
     * @param client The LLM client responsible for sending HTTP requests to the LLM.
     * @param modelName The name of the LLM model to be used for chat completions (e.g., "gemma-3-4b-it").
     */
    public ChatServices_LowLevel(LLM_Client client, String modelName) {
        this.client = client;
        this.modelName = modelName;
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
    public String generate(List<Message> messages, ModelParams params) throws LLMServiceException {
        try {
            String requestJson = buildRequest(messages, params, false);
            String response = client.sendRequest("v1/chat/completions", requestJson);
            return parseResponse(response);
        } catch (LLMServiceException e) {
            throw e;
        }
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
    public void generateStream(List<Message> messages, ModelParams params, StreamHandler handler) throws LLMServiceException {
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
    public void generateStreamRaw(String endpoint, String requestJson, StreamHandler handler) throws LLMServiceException {
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
            messagesNode.add(mapper.createObjectNode()
                    .put("role", msg.getRole().name().toLowerCase())
                    .put("content", msg.getContent()));
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
    private String parseResponse(String response) throws LLMParseException {
        try {
            JsonNode root = mapper.readTree(response);

            if (!root.has("choices") || !root.get("choices").isArray() || root.get("choices").isEmpty())
                throw new LLMParseException("Invalid response: missing or empty choices array");

            JsonNode firstChoice = root.path("choices").get(0);

            if (!firstChoice.has("message") || !firstChoice.path("message").has("content"))
                throw new LLMParseException("Invalid response: missing message content");

            return firstChoice.path("message").path("content").asText();
        } catch (JsonProcessingException e) {
            throw new LLMParseException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }
}
