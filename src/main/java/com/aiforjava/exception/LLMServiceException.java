package com.aiforjava.exception;

/**
 * A general exception for errors encountered while interacting with the LLM service.
 * This is a checked exception, meaning methods that throw it must declare it in their signature,
 * and callers must either catch it or re-throw it.
 */
public class LLMServiceException extends Exception {
    public LLMServiceException(String message) {
        super(message);
    }

    public LLMServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
