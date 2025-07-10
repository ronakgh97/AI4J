package com.aiforjava.exception;

/**
 * Custom exception for errors related to memory access and persistence operations.
 * This can include issues with reading from or writing to memory stores (e.g., files).
 */
public class MemoryAccessException extends RuntimeException {

    /**
     * Constructs a new MemoryAccessException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     */
    public MemoryAccessException(String message) {
        super(message);
    }

    /**
     * Constructs a new MemoryAccessException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause The cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MemoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MemoryAccessException with the specified cause and a detail message of (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of cause).
     *
     * @param cause The cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MemoryAccessException(Throwable cause) {
        super(cause);
    }
}
