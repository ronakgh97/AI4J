package com.aiforjava.exception;

/**
 * Custom runtime exception for errors encountered during LLM stream processing.
 * This exception wraps underlying checked exceptions like LLMParseException,
 * allowing them to be propagated through streams and lambdas without
 * requiring explicit checked exception handling in every lambda.
 */
public class LLMStreamProcessingException extends RuntimeException {

    /**
     * Constructs a new LLMStreamProcessingException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause The cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LLMStreamProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new LLMStreamProcessingException with the specified cause and a detail message of (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of cause).
     *
     * @param cause The cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public LLMStreamProcessingException(Throwable cause) {
        super(cause);
    }
}
