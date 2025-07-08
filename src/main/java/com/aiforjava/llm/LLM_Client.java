package com.aiforjava.llm;

/**
 * The LLM_Client interface defines the contract for interacting with Large Language Models (LLMs).
 * Implementations of this interface are responsible for sending requests to the LLM API
 * and handling the responses, whether they are standard (non-streaming) or streaming.
 * This abstraction allows for different HTTP client implementations to be used interchangeably.
 */
public interface LLM_Client {

    /**
     * Sends a non-streaming request to the LLM endpoint.
     *
     * @param endpoint The specific API endpoint to send the request to (e.g., "v1/chat/completions").
     * @param json The JSON request payload as a string.
     * @return The raw JSON response body as a string.
     * @throws com.aiforjava.exception.LLMServiceException If any error occurs during the request or response processing.
     */
    String sendRequest(String endpoint, String json) throws com.aiforjava.exception.LLMServiceException;

    /**
     * Sends a streaming request to the LLM endpoint.
     * The response content is delivered in chunks via the provided StreamHandler.
     *
     * @param endpoint The specific API endpoint to send the request to (e.g., "v1/chat/completions").
     * @param json The JSON request payload as a string.
     * @param handler A StreamHandler to process the incoming stream of content.
     * @throws com.aiforjava.exception.LLMServiceException If any error occurs during the request or response processing.
     */
    void sendStreamRequest(String endpoint, String json, StreamHandler handler) throws com.aiforjava.exception.LLMServiceException;
}


