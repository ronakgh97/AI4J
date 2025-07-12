
package com.aiforjava.llm.client;

import com.aiforjava.exception.Exception_Timeout;
import com.aiforjava.exception.LLMNetworkException;
import com.aiforjava.exception.LLMParseException;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.exception.LLMStreamProcessingException;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.streams.StreamHandler;
import com.aiforjava.llm.streams.StreamResponseParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * DefaultHttpClient is an implementation of the LLM_Client interface that uses Java's
 * built-in HttpClient to communicate with Large Language Models (LLMs) over HTTP.
 * It handles sending both standard (non-streaming) and streaming requests to the LLM endpoint.
 */
public class DefaultHttpClient implements LLM_Client, AutoCloseable {
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Duration timeout;
    private final String apiKey;
    private final boolean useBaseUrlAsEndpoint;
    private final StreamResponseParser streamResponseParser;
    private final long streamDelayMillis;
    private final ScheduledExecutorService scheduler;

    /**
     * Constructs a new DefaultHttpClient.
     *
     * @param baseUrl The base URL of the LLM API (e.g., "http://localhost:1234"). Must not be null.
     * @param timeout The maximum duration to wait for a connection and response. Must not be null.
     * @param apiKey  The API key needed for running online models like gemini, openAI, deepseek etc
     */
    public DefaultHttpClient(String baseUrl, Duration timeout, String apiKey) {
        this(baseUrl, timeout, apiKey, false, new DefaultStreamResponseParser(), 0L, Executors.newSingleThreadScheduledExecutor());
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
        this(baseUrl, timeout, apiKey, useBaseUrlAsEndpoint, new DefaultStreamResponseParser(), 0L, Executors.newSingleThreadScheduledExecutor());
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
        this(baseUrl, timeout, apiKey, useBaseUrlAsEndpoint, streamResponseParser, streamDelayMillis, Executors.newSingleThreadScheduledExecutor());
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
     * @param scheduler The ScheduledExecutorService to use for scheduling delayed tasks.
     */
    public DefaultHttpClient(String baseUrl, Duration timeout, String apiKey, boolean useBaseUrlAsEndpoint, StreamResponseParser streamResponseParser, long streamDelayMillis, ScheduledExecutorService scheduler) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "URL cannot be null");
        this.timeout = Objects.requireNonNull(timeout, "Timeout cannot be null");
        this.apiKey = apiKey; //Initialize API key for online models
        this.useBaseUrlAsEndpoint = useBaseUrlAsEndpoint;
        this.streamResponseParser = Objects.requireNonNull(streamResponseParser, "StreamResponseParser cannot be null");
        this.streamDelayMillis = streamDelayMillis;
        this.scheduler = Objects.requireNonNull(scheduler, "Scheduler cannot be null");
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

            try (Stream<String> stream = response.body()) {
                stream.forEach(line -> {
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
                                throw new LLMStreamProcessingException("Stream interrupted during delay", e);
                            }
                        }
                    } catch (LLMParseException e) {
                        throw new LLMStreamProcessingException("Error parsing LLM stream response", e);
                    }
                });
            }
        } catch (LLMStreamProcessingException e) {
            throw e; // Re-throw the specific stream processing exception
        } catch (HttpTimeoutException e) {
            throw new Exception_Timeout("Request timed out after " + timeout.toSeconds() + " seconds", e);
        } catch (IOException e) {
            throw new LLMNetworkException("Network error communicating with " + baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMNetworkException("Request was Interrupted", e);
        }
    }

    /**
     * Sends a streaming HTTP POST request to the LLM endpoint asynchronously.
     * The response is processed line by line by the provided StreamHandler, with delays
     * introduced using a ScheduledExecutorService to avoid blocking the calling thread.
     *
     * @param endpoint The specific API endpoint (e.g., "v1/chat/completions").
     * @param json The JSON request payload as a string.
     * @param handler A StreamHandler to process each chunk of the streaming response.
     * @return A CompletableFuture that completes when the stream processing is finished or an error occurs.
     * @throws LLMServiceException If an HTTP error occurs during the initial request setup.
     * @throws Exception_Timeout If the initial request times out.
     * @throws LLMNetworkException If a network error occurs during the initial request setup.
     */
    public CompletableFuture<Void> sendStreamRequestAsync(String endpoint, String json, StreamHandler handler) {
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

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new CompletionException(new LLMServiceException("HTTP error: " + response.statusCode()));
                    }
                    return response.body();
                })
                .thenCompose(stream -> {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    AtomicBoolean isCancelled = new AtomicBoolean(false);

                    // Convert stream to an iterator to process elements one by one
                    java.util.Iterator<String> iterator = stream.iterator();

                    // Ensure the stream is closed when the future completes
                    future.whenComplete((v, ex) -> stream.close());

                    // Recursive function to process stream elements with delay
                    processStreamElement(iterator, handler, future, isCancelled);

                    return future;
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof HttpTimeoutException) {
                        throw new CompletionException(new Exception_Timeout("Request timed out after " + timeout.toSeconds() + " seconds", cause));
                    } else if (cause instanceof IOException) {
                        throw new CompletionException(new LLMNetworkException("Network error communicating with " + baseUrl, cause));
                    } else if (cause instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(new LLMNetworkException("Request was Interrupted", cause));
                    } else if (cause instanceof LLMServiceException) {
                        throw new CompletionException((LLMServiceException) cause);
                    } else if (cause instanceof LLMStreamProcessingException) {
                        throw (LLMStreamProcessingException) cause;
                    } else {
                        throw new CompletionException("Unexpected error during async stream request", cause);
                    }
                });
    }

    private void processStreamElement(java.util.Iterator<String> iterator, StreamHandler handler, CompletableFuture<Void> future, AtomicBoolean isCancelled) {
        if (!iterator.hasNext() || isCancelled.get()) {
            future.complete(null);
            return;
        }

        String line = iterator.next();

        CompletableFuture.runAsync(() -> {
            try {
                String content = streamResponseParser.parse(line);
                if (content != null) {
                    handler.onStream(content);
                }
            } catch (LLMParseException e) {
                isCancelled.set(true);
                future.completeExceptionally(new LLMStreamProcessingException("Error parsing LLM stream response", e));
            } catch (Exception e) {
                isCancelled.set(true);
                future.completeExceptionally(new LLMNetworkException("Error during stream processing", e));
            }
        }, scheduler).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        isCancelled.set(true);
                        future.completeExceptionally(ex);
                    } else {
                        if (streamDelayMillis > 0) {
                            scheduler.schedule(() -> processStreamElement(iterator, handler, future, isCancelled), streamDelayMillis, TimeUnit.MILLISECONDS);
                        } else {
                            processStreamElement(iterator, handler, future, isCancelled);
                        }
                    }
                });
    }

    /**
     * Shuts down the internal ScheduledExecutorService, releasing its resources.
     * This method should be called when the DefaultHttpClient instance is no longer needed.
     */
    @Override
    public void close() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
