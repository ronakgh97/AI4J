package com.aiforjava.memory.ChatLogger;

import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An enhanced implementation of {@link MemoryManager} that provides caching for file-based chat history.
 * Messages are stored in an in-memory list for fast access, and changes are periodically flushed
 * to a JSON file. This improves performance compared to reading/writing the file on every operation.
 */
public class CachedFileMemory implements MemoryManager {

    private static final Logger logger = LoggerFactory.getLogger(CachedFileMemory.class);
    private static final String DEFAULT_LOG_DIRECTORY = "chat_logs";
    private static final String DEFAULT_LOG_FILE_NAME = "chat_memory.json";

    private final Path logFilePath;
    private final ObjectMapper objectMapper;
    private final List<Message> messagesCache;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs a new CachedFileMemory with a default log directory and file name.
     * The log file will be created in the application's current working directory.
     */
    public CachedFileMemory() {
        this(DEFAULT_LOG_DIRECTORY, DEFAULT_LOG_FILE_NAME);
    }

    /**
     * Constructs a new CachedFileMemory with a specified log directory and file name.
     *
     * @param logDirectory The directory where the chat log file will be stored.
     * @param logFileName The name of the chat log file.
     */
    public CachedFileMemory(String logDirectory, String logFileName) {
        this.logFilePath = Paths.get(logDirectory, logFileName);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.messagesCache = new ArrayList<>();
        loadHistoryFromFile();
    }

    /**
     * Adds a new message to the in-memory cache.
     * The changes are not immediately persisted to the file; call {@link #flush()} to save them.
     *
     * @param message The Message object to be added.
     */
    @Override
    public void addMessage(Message message) {
        lock.writeLock().lock();
        try {
            messagesCache.add(message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new message to the in-memory cache and immediately persists the updated history to the file.
     * This ensures data is saved after each message addition.
     *
     * @param message The Message object to be added.
     */
    public void addMessageAndSave(Message message) {
        lock.writeLock().lock();
        try {
            messagesCache.add(message);
            saveHistoryToFile(); // Save on every add for simplicity
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Message> getMessagesList() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(messagesCache));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            messagesCache.clear();
            saveHistoryToFile(); // Clear file as well
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Flushes the current in-memory chat history to the JSON file.
     * This method should be called periodically or on application shutdown
     * to ensure that all changes are persisted.
     */
    public void flush() {
        lock.writeLock().lock();
        try {
            saveHistoryToFile();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Loads chat history from the JSON file into the in-memory cache.
     * If the file does not exist or is empty, the cache remains empty.
     */
    private void loadHistoryFromFile() {
        if (Files.exists(logFilePath) && Files.isReadable(logFilePath)) {
            try {
                String json = Files.readString(logFilePath);
                if (!json.trim().isEmpty()) {
                    messagesCache.addAll(objectMapper.readValue(json, new TypeReference<List<Message>>() {}));
                }
            } catch (IOException e) {
                logger.error("Error loading chat history from file: {}", logFilePath, e);
            }
        }
    }

    /**
     * Saves the current in-memory chat history to the JSON file.
     * The directory will be created if it does not exist.
     */
    private void saveHistoryToFile() {
        try {
            Files.createDirectories(logFilePath.getParent());
            objectMapper.writeValue(logFilePath.toFile(), messagesCache);
        } catch (IOException e) {
            logger.error("Error saving chat history to file: {}", logFilePath, e);
        }
    }
}