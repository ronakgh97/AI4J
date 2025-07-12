package com.aiforjava.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class MasterKeyManager {

    private static final File MASTER_KEYS_FILE = new File("master_keys.txt");
    private final Set<String> validMasterKeys = new HashSet<>();

    public MasterKeyManager() {
        loadMasterKeys();
    }

    private void loadMasterKeys() {
        if (!MASTER_KEYS_FILE.exists()) {
            // Create the file with a default key if it doesn't exist
            try (PrintWriter writer = new PrintWriter(new FileWriter(MASTER_KEYS_FILE))) {
                writer.println("deploytest"); // Default master key
                System.out.println("Created master_keys.txt with default key: deploytest");
            } catch (IOException e) {
                System.err.println("Error creating master_keys.txt: " + e.getMessage());
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(MASTER_KEYS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String key = line.trim();
                if (!key.isEmpty()) {
                    validMasterKeys.add(key);
                }
            }
            System.out.println("Loaded " + validMasterKeys.size() + " master keys.");
        } catch (IOException e) {
            System.err.println("Error loading master keys from file: " + e.getMessage());
        }
    }

    public boolean isValidMasterKey(String key) {
        return validMasterKeys.contains(key);
    }

    public synchronized void consumeMasterKey(String key) {
        if (validMasterKeys.remove(key)) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(MASTER_KEYS_FILE))) {
                for (String remainingKey : validMasterKeys) {
                    writer.println(remainingKey);
                }
                System.out.println("Master key '" + key + "' consumed and removed from file.");
            } catch (IOException e) {
                System.err.println("Error consuming master key and updating file: " + e.getMessage());
            }
        }
    }
}
