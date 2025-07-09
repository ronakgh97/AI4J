
package com.aiforjava.llm;

import com.aiforjava.exception.Exception_Timeout;
import com.aiforjava.exception.LLMNetworkException;
import com.aiforjava.exception.LLMParseException;
import com.aiforjava.exception.LLMServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * DefaultHttpClient is an implementation of the LLM_Client interface that uses Java's
 * built-in HttpClient to communicate with Large Language Models (LLMs) over HTTP.
 * It handles sending both standard (non-streaming) and streaming requests to the LLM endpoint.
 */
public class DefaultHttpClient implements LLM_Client {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Duration timeout;
    private final String apiKey;
    private final boolean useBaseUrlAsEndpoint;
    private final StreamResponseParser streamResponseParser;
    private final long streamDelayMillis;

    /**
     * Constructs a new DefaultHttpClient.
     *
     * @param baseUrl The base URL of the LLM API (e.g., "http://localhost:1234"). Must not be null.
     * @param timeout The maximum duration to wait for a connection and response. Must not be null.
     * @param apiKey  The API key needed for running online models like gemini, openAI, deepseek etc
     */
    public DefaultHttpClient(String baseUrl, Duration timeout, String apiKey) {
        this(baseUrl, timeout, apiKey, false, new DefaultStreamResponseParser(), 0L); // Default to no delay
    }

    /**
     * Constructs a new DefaultHttpClient.
     *
     * @param baseUrl The base URL of the LLM API (e.g., "http://localhost:1234"). Must not be null.
     * @param timeout The maximum duration to wait for a connection and response. Must not be null.
     * @param apiKey  The API key needed for running online models like gemini, openAI, deepseek etc
     * @param useBaseUrlAsEndpoint If true, the baseUrl will be used as the full endpoint URI, ignoring the 'endpoint' parameter for path concatenation.
     */
    public DefaultHttpClient(String baseUrl, Duration timeout, String apiKey, boolean useBaseUrlAsEndpoint) {
        this(baseUrl, timeout, apiKey, useBaseUrlAsEndpoint, new DefaultStreamResponseParser(), 0L); // Default to no delay
    }

    /**
     * Constructs a new DefaultHttpClient.
     *
     * @param baseUrl The base URL of the LLM API (e.g., "http://localhost:1234"). Must not be null.
     * @param timeout The maximum duration to wait for a connection and response. Must not be null.
     * @param apiKey  The API key needed for running online models like gemini, openAI, deepseek etc
     * @param useBaseUrlAsEndpoint If true, the baseUrl will be used as the full endpoint URI, ignoring the 'endpoint' parameter for path concatenation.
     * @param streamResponseParser The parser to use for streaming responses.
     * @param streamDelayMillis The delay in milliseconds between processing each stream chunk for smoother output.
     */
    public DefaultHttpClient(String baseUrl, Duration timeout, String apiKey, boolean useBaseUrlAsEndpoint, StreamResponseParser streamResponseParser, long streamDelayMillis) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "URL cannot be null");
        this.timeout = Objects.requireNonNull(timeout, "Timeout cannot be null");
        this.apiKey = apiKey; //Initialize API key for online models
        this.useBaseUrlAsEndpoint = useBaseUrlAsEndpoint;
        this.streamResponseParser = Objects.requireNonNull(streamResponseParser, "StreamResponseParser cannot be null");
        this.streamDelayMillis = streamDelayMillis;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * Sends a non-streaming HTTP POST request to the LLM endpoint.
     *
     * @param endpoint The specific API endpoint (e.g., "v1/chat/completions").
     * @param json The JSON request payload as a string.
     * @return The raw JSON response body as a string.
     * @throws LLMServiceException If an HTTP error occurs (e.g., status code >= 400).
     * @throws Exception_Timeout If the request times out.
     * @throws LLMNetworkException If a network error occurs or the request is interrupted.
     */
    @Override
    public String sendRequest(String endpoint, String json) throws LLMServiceException {
        try {
            // Construct the URI based on whether the base URL should be used as the full endpoint
            String uriString = useBaseUrlAsEndpoint ? baseUrl : baseUrl + "/" + endpoint;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(uriString))
                    .timeout(timeout)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json");

            if(apiKey!=null && !apiKey.isEmpty())
                requestBuilder.header("Authorization", "Bearer " + apiKey);

            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new LLMServiceException("HTTP error: " + response.statusCode() + " : " + response.body());
            }

            return response.body();

        } catch (HttpTimeoutException e) {
            throw new Exception_Timeout("Request timed out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new LLMNetworkException("Network error communicating with " + baseUrl, e);
        } catch (IllegalArgumentException e) {
            throw new LLMServiceException("Invalid request argument: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMNetworkException("Request was Interrupted", e);
        }
    }

    /**
     * Sends a streaming HTTP POST request to the LLM endpoint.
     * The response is processed line by line by the provided StreamHandler.
     *
     * @param endpoint The specific API endpoint (e.g., "v1/chat/completions").
     * @param json The JSON request payload as a string.
     * @param handler A StreamHandler to process each chunk of the streaming response.
     * @throws LLMServiceException If an HTTP error occurs.
     * @throws Exception_Timeout If the request times out.
     * @throws LLMNetworkException If a network error occurs or the request is interrupted.
     * @throws RuntimeException wrapping an {@link LLMParseException} if JSON parsing fails within the stream.
     */
    @Override
    public void sendStreamRequest(String endpoint, String json, StreamHandler handler) throws LLMServiceException {
        try {
            String uriString = useBaseUrlAsEndpoint ? baseUrl : baseUrl + "/" + endpoint;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(uriString))
                    .timeout(timeout)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json");

            if(apiKey!=null && !apiKey.isEmpty())
                requestBuilder.header("Authorization", "Bearer " + apiKey);

            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() >= 400) {
                throw new LLMServiceException("HTTP error: " + response.statusCode());
            }

            response.body().forEach(line -> {
                try {
                    String content = streamResponseParser.parse(line);
                    if (content != null) {
                        handler.onStream(content);
                    }
                    if (streamDelayMillis > 0) {
                        try {
                            Thread.sleep(streamDelayMillis);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(new LLMNetworkException("Stream interrupted during delay", e));
                        }
                    }
                } catch (LLMParseException e) {
                    throw new RuntimeException(e); // Wrap checked exception in RuntimeException for lambda
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof LLMParseException) {
                throw (LLMParseException) e.getCause();
            } else {
                throw e;
            }
        } catch (HttpTimeoutException e) {
            throw new Exception_Timeout("Request timed out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new LLMNetworkException("Network error communicating with " + baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMNetworkException("Request was Interrupted", e);
        }
    }
}
