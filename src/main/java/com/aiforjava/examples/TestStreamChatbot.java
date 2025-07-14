package com.aiforjava.examples;

import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.streams.StreamHandler;
import com.aiforjava.llm.streams.StreamResponse;
import com.aiforjava.memory.memory_algorithm.SlidingWindowMemory;

import java.time.Duration;
import java.util.Scanner;

public class TestStreamChatbot {

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static final String MODEL_NAME = "qwen/qwen3-4b";

    public static void main(String[] args) {
        LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local");
        ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, MODEL_NAME);

        SlidingWindowMemory memory = new SlidingWindowMemory(10);

        PromptTemplate promptTemplate = new PromptTemplate(
            "You are a helpful AI assistant",
            "{user_message}"
        );

        ChatServices chatService = new ChatServices(
            lowLevelChatService,
            memory,
            new ModelParams.Builder().setTemperature(0.7).setMaxTokens(512).build(),
            promptTemplate
        );

        System.out.println("TestStreamChatbot started. Type 'exit' to quit.");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nYou: ");
            String userMessage = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userMessage)) {
                break;
            }

            try {
                System.out.println("\n--- AI Response Stream ---");
                final boolean[] contentStarted = {false};

                chatService.chatStream(userMessage, response -> {
                    if (response.getReasoningContent() != null) {
                        System.out.print(response.getReasoningContent());
                    }
                    
                    if (response.getContent() != null) {
                        if (!contentStarted[0]) {
                            System.out.print("\n\n--- Content ---\n");
                            contentStarted[0] = true;
                        }
                        System.out.print(response.getContent());
                    }
                });

                System.out.println("\n--- End of Stream ---");

            } catch (Exception e) {
                System.err.println("\nError during streaming: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scanner.close();
        System.out.println("Chatbot exited.");
    }
}
