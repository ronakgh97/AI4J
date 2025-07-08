
package com.aiforjava.exception;

/**
 * Represents an exception that occurs due to network-related issues when communicating with the LLM service.
 * This could include problems like connection timeouts, DNS resolution failures, or other I/O errors
 * during the HTTP request. This provides a more specific error type than the general {@link LLMServiceException}.
 */
public class LLMNetworkException extends LLMServiceException {

    public LLMNetworkException(String message) {
        super(message);
    }

    public LLMNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}

