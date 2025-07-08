
package com.aiforjava.memory.ChatLogs;

import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * FileMemory is an implementation of the MemoryManager interface that persists
 * chat messages to a JSON file. This allows conversation history to be saved
 * and loaded across application runs, providing persistent memory for chatbots.
 * The chat history file is stored in a designated directory, defaulting to "chat_logs".
 */
public class FileMemory implements MemoryManager {
    private static final String DEFAULT_HISTORY_DIR = "chat_logs";
    private static final String HISTORY_FILE = "chat_history.json";
    private final Path historyFilePath;
    private final ObjectMapper mapper;

    /**
     * Constructs a FileMemory instance using the default history directory.
     * The chat history will be saved in a 'chat_logs' folder relative to the application's execution path.
     */
    public FileMemory() {
        this(DEFAULT_HISTORY_DIR);
    }

    /**
     * Constructs a FileMemory instance with a specified base directory for history files.
     *
     * @param baseDirectory The base directory where the chat history file will be stored.
     *                      If this directory does not exist, it will be created.
     */
    public FileMemory(String baseDirectory) {
        Path baseDirPath = Paths.get(baseDirectory);
        this.historyFilePath = baseDirPath.resolve(HISTORY_FILE);

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            Files.createDirectories(baseDirPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create history directory: " + baseDirectory, e);
        }
    }

    /**
     * Adds a new message to the chat history and persists the updated history to the file.
     *
     * @param message The Message object to be added.
     */
    @Override
    public void addMessage(Message message) {
        List<Message> messages = getMessagesList();
        messages.add(message);
        try {
            mapper.writeValue(historyFilePath.toFile(), messages);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to history file: " + historyFilePath, e);
        }
    }

    /**
     * Retrieves the current list of messages from the chat history file.
     *
     * @return A List of Message objects representing the conversation history.
     *         Returns an empty list if the history file does not exist or cannot be read.
     */
    @Override
    public List<Message> getMessagesList() {
        if (!Files.exists(historyFilePath)) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(historyFilePath.toFile(), new TypeReference<List<Message>>() {});
        } catch (IOException e) {
            System.err.println("Warning: Could not read chat history from " + historyFilePath + ". Starting with empty history. Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Clears the entire chat history by deleting the history file.
     * If the file does not exist, no action is taken.
     */
    @Override
    public void clear() {
        try {
            Files.deleteIfExists(historyFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to clear history file: " + historyFilePath, e);
        }
    }
}

