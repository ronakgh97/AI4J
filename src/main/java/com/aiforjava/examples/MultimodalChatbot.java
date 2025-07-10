package com.aiforjava.examples;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.memory_algorithm.OptimizedSlidingWindowMemory;
import com.aiforjava.memory.MemoryManager;

import java.io.File;
import java.time.Duration;

public class MultimodalChatbot {

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static final String MODEL_NAME = "google/gemma-3-4b"; // Or your preferred multimodal model
    private static final int MEMORY_WINDOW_SIZE = 50;

    public static void main(String[] args) {
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, MODEL_NAME);
        MemoryManager memory = new OptimizedSlidingWindowMemory(MEMORY_WINDOW_SIZE);
        PromptTemplate promptTemplate = new PromptTemplate("You are a helpful, friendly AI assistant.", "User: {user_message}\nAI:");

        ChatServices chatService = new ChatServices(
                lowLevelChatService,
                memory,
                new ModelParams.Builder().build(), // Default model params
                promptTemplate
        );

        System.out.println("\n--- Multimodal Chatbot Examples ---\n");

        // Scenario 1: Text with Image
        System.out.println("\n--- Scenario 1: Text with Image ---");
        File imageFile = new File("C:\\tmp\\abc.jpg"); // <<< IMPORTANT: Replace with a valid image path
        if (!imageFile.exists()) {
            System.err.println("Error: Image file not found at " + imageFile.getAbsolutePath());
            System.err.println("Please update MultimodalChatbot.java with a valid image path.");
        } else {
            try {
                System.out.print("You (with image): What is in this picture?\nAI: ");
                chatService.chatStream("What is in this picture?", imageFile, System.out::print);
                System.out.println();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        // Scenario 2: Text with no_think
        System.out.println("\n--- Scenario 2: Text with no_think ---");
        // To disable thinking, append /no_think to the prompt
        try {
            System.out.print("You (no think): Tell me a short, direct fact about Java. /no_think\nAI: ");
            chatService.chatStream("Tell me a short, direct fact about Java. /no_think", new ModelParams.Builder().build(), System.out::print);
            System.out.println();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        // Scenario 3: Text with Image and no_think
        System.out.println("\n--- Scenario 3: Text with Image and no_think ---");
        if (!imageFile.exists()) {
            System.err.println("Skipping Scenario 3: Image file not found.");
        } else {
            // To disable thinking, append /no_think to the prompt
            try {
                System.out.print("You (with image, no think): Describe this image very briefly. /no_think\nAI: ");
                chatService.chatStream("Describe this image very briefly. /no_think", imageFile, new ModelParams.Builder().build(), System.out::print);
                System.out.println();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        System.out.println("\n--- Multimodal Chatbot Examples Finished ---\n");
    }
}