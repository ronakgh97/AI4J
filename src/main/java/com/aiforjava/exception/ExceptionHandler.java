
package com.aiforjava.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A utility class for handling exceptions in a centralized manner.
 * This class provides a static method to process and log exceptions.
 * For production environments, consider integrating a robust logging framework like SLF4J with Logback or Log4j2.
 */
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Handles a given exception by logging its message and stack trace using SLF4J.
     *
     * @param e The exception to handle. It should not be null.
     */
    public static void handle(Exception e) {
        if (e == null) {
            logger.error("An attempt was made to handle a null exception.");
            return;
        }
        logger.error("An error occurred: {} - {}", e.getClass().getName(), e.getMessage(), e);
    }
}

