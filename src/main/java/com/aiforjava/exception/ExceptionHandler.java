
package com.aiforjava.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A utility class for handling exceptions in a centralized manner.
 * This class provides a static method to process and log exceptions.
 * For production environments, consider integrating a robust logging framework like SLF4J with Logback or Log4j2.
 */
public class ExceptionHandler {

    /**
     * Handles a given exception by printing its message and stack trace to the standard error stream.
     *
     * @param e The exception to handle. It should not be null.
     */
    public static void handle(Exception e) {
        if (e == null) {
            System.err.println("An attempt was made to handle a null exception.");
            return;
        }
        System.err.println("An error occurred: " + e.getClass().getName() + " - " + e.getMessage());
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        System.err.println(sw.toString());
    }
}

