package com.aiforjava.memory.cache;


import com.aiforjava.llm.client.LLMResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Objects;

/**
 * Manages caching of LLM responses using Caffeine. This class provides a centralized
 * mechanism to store and retrieve LLM responses, reducing redundant API calls and improving performance.
 */
public class LLMCacheManager {

    private final Cache<String, LLMResponse> llmResponseCache;

    /**
     * Constructs an LLMCacheManager with default caching parameters.
     * Default: maximum size of 1000 entries, expire after write of 10 minutes.
     */
    public LLMCacheManager() {
        this(1000, Duration.ofMinutes(10));
    }

    /**
     * Constructs an LLMCacheManager with custom caching parameters.
     *
     * @param maximumSize The maximum number of entries the cache can hold.
     * @param expireAfterWrite The duration after which an entry expires since its last write.
     */
    public LLMCacheManager(long maximumSize, Duration expireAfterWrite) {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("Maximum size must be positive.");
        }
        Objects.requireNonNull(expireAfterWrite, "Expire after write duration cannot be null.");

        this.llmResponseCache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWrite)
                .build();
    }

    /**
     * Retrieves an LLM response from the cache.
     *
     * @param key The cache key (e.g., a hash of the prompt and model parameters).
     * @return The cached LLMResponse, or null if not found.
     */
    public LLMResponse get(String key) {
        return llmResponseCache.getIfPresent(key);
    }

    /**
     * Stores an LLM response in the cache.
     *
     * @param key The cache key.
     * @param value The LLMResponse to cache.
     */
    public void put(String key, LLMResponse value) {
        llmResponseCache.put(key, value);
    }

    /**
     * Invalidates a specific entry from the cache.
     *
     * @param key The key of the entry to invalidate.
     */
    public void invalidate(String key) {
        llmResponseCache.invalidate(key);
    }

    /**
     * Clears all entries from the cache.
     */
    public void invalidateAll() {
        llmResponseCache.invalidateAll();
    }
}
