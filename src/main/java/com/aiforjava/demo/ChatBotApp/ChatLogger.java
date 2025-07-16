
package com.aiforjava.demo.ChatBotApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatLogger {

    private final String filePath;
    private final List<LogEntry> logEntries;
    private final ObjectMapper objectMapper;

    public ChatLogger(String filePath) {
        this.filePath = filePath;
        this.logEntries = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void log(String role, String message) {
        logEntries.add(new LogEntry(role, message));
        flush();
    }

    private void flush() {
        try {
            objectMapper.writeValue(new File(filePath), logEntries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class LogEntry {
        private final LocalDateTime timestamp;
        private final String role;
        private final String message;

        public LogEntry(String role, String message) {
            this.timestamp = LocalDateTime.now();
            this.role = role;
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getRole() {
            return role;
        }

        public String getMessage() {
            return message;
        }
    }
}
