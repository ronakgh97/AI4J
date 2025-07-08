package com.aiforjava.examples;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.exception.LLMServiceException;
import java.time.Duration;
import java.util.*;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.DefaultHttpClient;
import com.aiforjava.llm.LLM_Client;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.memory.SlidingWindowMemory;

/**
 * This example demonstrates a high-level chatbot implementation using AI4J's `ChatServices`
 * with `SlidingWindowMemory`. This setup allows the chatbot to maintain a configurable
 * conversational history, making it suitable for multi-turn interactions where context
 * is important.
 */
public class SimpleChatbot {
    public static void main(String[] args) throws InterruptedException {

        // 1. Initialize the LLM Client and Low-Level Chat Services
        LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90),"local");
        ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");

        // 2. Define Model Parameters
        ModelParams params = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(256)
                .setTopP(0.95)
                .build();

        // 3. Create Memory Manager
        MemoryManager memory = new SlidingWindowMemory(20);

        // 4. Create High-Level Chat Service
        PromptTemplate promptTemplate = new PromptTemplate("You are AI Assistant. Keep responses concise (1-2 sentences max).", "User: {user_message}\nAI:");
        ChatServices chatService = new ChatServices(
                llm,
                memory,
                params,
                promptTemplate
        );

        Scanner scanner = new Scanner(System.in);
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║           AI CHATBOT - JAVA FRAMEWORK              ║");
        System.out.println("║     Type 'exit' to quit, 'reset' to start over     ║");
        System.out.println("╚════════════════════════════════════════════════════╝");

        chatService.reset();

        // 5. Main Chat Loop
        while (true) {
            System.out.print("\nYou:-> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("reset")) {
                chatService.reset();
                System.out.println("Chat history cleared. New conversation started.");
                continue;
            }

            try {
                System.out.print("LLM: ");
                chatService.chatStream(input, System.out::print);
                System.out.println();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
        scanner.close();
    }
}

