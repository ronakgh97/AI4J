
package com.aiforjava.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for handling exceptions in a centralized manner.
 * This class provides static methods to process and log exceptions.
 * It uses SLF4J for logging, which can be configured with a logging framework like Logback or Log4j2.
 */
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * Handles a given exception by logging its message and stack trace using SLF4J.
     * This is a convenience method that delegates to the more detailed handle method.
     *
     * @param e The exception to handle. It should not be null.
     */
    public static void handle(Exception e) {
        handle(e, null, null);
    }

    /**
     * Handles a given exception by logging its message and stack trace using SLF4J,
     * including the class and method where the exception occurred.
     *
     * @param e The exception to handle. It should not be null.
     * @param className The name of the class where the exception occurred. Can be null.
     * @param methodName The name of the method where the exception occurred. Can be null.
     */
    public static void handle(Exception e, String className, String methodName) {
        if (e == null) {
            logger.error("handle called with a null exception.");
            return;
        }

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("An error occurred: ").append(e.getClass().getName());

        if (className != null && !className.isEmpty()) {
            errorMessage.append(" in class: ").append(className);
        }

        if (methodName != null && !methodName.isEmpty()) {
            errorMessage.append(", method: ").append(methodName);
        }

        errorMessage.append(". Message: ").append(e.getMessage());

        logger.error(errorMessage.toString(), e);
    }
}

