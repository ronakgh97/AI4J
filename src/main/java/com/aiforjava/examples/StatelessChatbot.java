package com.aiforjava.examples;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.DefaultHttpClient;
import com.aiforjava.llm.LLM_Client;
import com.aiforjava.llm.ModelParams;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

/**
 * This example demonstrates a stateless chatbot implementation using the low-level
 * `ChatServices_LowLevel` from AI4J. Since it's stateless, each turn of the conversation
 * is independent, and no previous messages are remembered by the LLM.
 * This is useful for single-turn queries or when memory management is handled externally.
 */
public class StatelessChatbot {

    public static void main(String[] args) {
        LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90),"local");
        ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");

        ModelParams params = new ModelParams.Builder()
                .setTemperature(0.7)
                .setMaxTokens(256)
                .setTopP(0.95)
                .build();

        String systemPrompt = "You are a helpful, friendly assistant. Keep responses concise (1-2 sentences max).";

        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║           STATELESS CHATBOT - NO MEMORY           ║");
        System.out.println("║              Type 'exit' to quit                  ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");

        while (true) {
            System.out.print("\nYou:-> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            List<Message> messages = List.of(
                    new Message(MessageRole.SYSTEM, systemPrompt),
                    new Message(MessageRole.USER, input)
            );

            try {
                System.out.print("LLM: ");
                llm.generateStream(messages, params, System.out::print);
                System.out.println();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
        scanner.close();
    }
}

