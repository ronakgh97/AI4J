package com.aiforjava.exception;

/**
 * Custom runtime exception to indicate that a timeout occurred during an operation.
 * This is an unchecked exception, meaning it does not need to be explicitly
 * caught or declared in method signatures. It is typically used when an
 * operation fails to complete within a specified time limit.
 */
public class Exception_Timeout extends RuntimeException {

    /**
     * Constructs a new Exception_Timeout with the specified detail message and cause.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause The cause of the timeout, which is saved for later retrieval by the {@link #getCause()} method.
     *              A {@code null} value is permitted and indicates that the cause is nonexistent or unknown.
     */
    public Exception_Timeout(String message, Throwable cause) {
        super(message, cause);
    }
}
