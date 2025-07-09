package com.aiforjava.examples;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.DefaultHttpClient;
import com.aiforjava.llm.DefaultStreamResponseParser;
import com.aiforjava.llm.LLM_Client;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.memory.MemoryManager;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;
import com.aiforjava.memory.ChatLogs.FileMemory;
import com.aiforjava.memory.ChatLogs.CachedFileMemory;
import java.time.Duration;
import java.util.Scanner;

/**
 * This example demonstrates a low-level chatbot implementation that utilizes `FileMemory`
 * for persisting chat history. This means the conversation is saved to a file and can be
 * resumed even after the application is closed and restarted. It uses the low-level
 * `ChatServices_LowLevel` for direct interaction with the LLM.
 */
public class ChatBot {
    public static void main(String[] args) {

        // 1. Initialize the LLM Client and Low-Level Chat Services
        LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90),"local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");

        // 2. Define Model Parameters
        ModelParams params = new ModelParams.Builder()
                .setTemperature(0.9)
                .setMaxTokens(1024)
                .setTopP(0.95)
                .build();

        // 3. Initialize Memory Manager with FileMemory
        // MemoryManager memory = new FileMemory();
        MemoryManager memory = new com.aiforjava.memory.ChatLogs.CachedFileMemory();

        // Uncomment the line below to clear the history at the start of the conversation
        // memory.clear();

        if (memory.getMessagesList().isEmpty()) {
            memory.addMessage(new Message(MessageRole.SYSTEM, "You are AI Assistant"));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═════════════════════════════════════════════════════╗");
        System.out.println("║          LOW-LEVEL CHATBOT - FILE MEMORY            ║");
        System.out.println("║     Type 'exit' to quit, 'clear' to clear memory    ║");
        System.out.println("╚═════════════════════════════════════════════════════╝");

        // 4. Main Chat Loop
        while (true) {
            System.out.print("\nYou:-> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("clear")) {
                memory.clear();
                System.out.println("Chat history cleared.");
                memory.addMessage(new Message(MessageRole.SYSTEM, "You are AI Assistant"));
                continue;
            }

            memory.addMessage(new Message(MessageRole.USER, input));

            try {
                System.out.print("LLM->:  ");
                llm.generateStream(memory.getMessagesList(),params, System.out::print);
                System.out.println();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
        scanner.close();
    }
}

