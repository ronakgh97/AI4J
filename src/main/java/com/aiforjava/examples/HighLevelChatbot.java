package com.aiforjava.examples;

import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.MemoryManager;

import java.time.Duration;
import java.util.Scanner;

/**
 * Demonstrates a high-level chatbot using ChatServices with SlidingWindowMemory.
 * This chatbot maintains a conversational history within a fixed-size window,
 * allowing for context-aware interactions.
 *
 * To run this example:
 * 1. Ensure you have a local LLM server (e.g., LM Studio, Ollama) running and
 *    serving a compatible model (e.g., `gemma-3-4b-it`) on `http://localhost:1234`.
 * 2. Execute the following Maven command from the project root:
 *    {@code mvn exec:java -Dexec.mainClass="com.aiforjava.examples.HighLevelChatbot"}
 */
public class HighLevelChatbot {

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static final String MODEL_NAME = "google/gemma-3-1b"; // Or your preferred model
    private static final int MEMORY_WINDOW_SIZE = 10; // Keep last 10 messages in memory

    public static void main(String[] args) {
        // Initialize LLM client with a timeout
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90),"local", false, new DefaultStreamResponseParser(), 50L);

        // Initialize low-level chat service
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, MODEL_NAME);

        // Configure model parameters (optional)
        ModelParams params = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(512)
                .setTopP(0.9)
                .build();

        // Initialize memory manager (Sliding Window Memory)
        // MemoryManager memory = new SlidingWindowMemory(MEMORY_WINDOW_SIZE);
        // MemoryManager memory = new OptimizedSlidingWindowMemory(MEMORY_WINDOW_SIZE);
        MemoryManager memory = new com.aiforjava.memory.ChatLogger.CachedFileMemory();

        // Initialize prompt template
        PromptTemplate promptTemplate = new PromptTemplate("You are a helpful AI assistant. Keep your responses concise and to the point.", "User: {user_message}\nAI:");

        // Initialize high-level chat service with memory and a system prompt
        ChatServices chatService = new ChatServices(
                lowLevelChatService,
                memory,
                params,
                promptTemplate
        );

        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║        HIGH LEVEL CHATBOT - JAVA FRAMEWORK        ║");
        System.out.println("║     Type 'exit' to quit, 'reset' to start over    ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");

        while (true) {
            System.out.print("You: ");
            String userMessage = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userMessage)) {
                System.out.println("Exiting chatbot. Goodbye!");
                break;
            }

            System.out.print("AI: ");
            try {
                // Use the high-level chat service to get a streaming response
                chatService.chatStream(userMessage, System.out::print);
                System.out.println(); // New line after AI's response
            } catch (Exception e) {
                System.err.println("Error during chat: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}
