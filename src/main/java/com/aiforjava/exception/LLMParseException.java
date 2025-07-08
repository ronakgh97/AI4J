package com.aiforjava.exception;

/**
 * Exception specifically for errors encountered during parsing of LLM responses.
 * This extends LLMServiceException, providing a more granular error type for parsing issues.
 */
public class LLMParseException extends LLMServiceException {
    public LLMParseException(String message) {
        super(message);
    }

    public LLMParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
