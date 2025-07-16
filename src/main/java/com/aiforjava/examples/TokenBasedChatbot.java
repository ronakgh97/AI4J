package com.aiforjava.examples;

import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.Chat.HighLevel.ChatServices;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.Prompt.PromptTemplate;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.client.LLMResponse;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.streams.StreamHandler;
import com.aiforjava.memory.memory_algorithm.TokenCountingMemory;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;
import com.aiforjava.util.TokenCalculator;

import java.time.Duration;
import java.util.Scanner;

public class TokenBasedChatbot {

    private static final String LLM_BASE_URL = "http://localhost:1234";
    private static final String MODEL_NAME = "google/gemma-3-1b"; // Or your preferred model
    private static final int MAX_MEMORY_TOKENS = 500; // Max tokens for conversation history

    public static void main(String[] args) {
        try {
            LLM_Client client = new DefaultHttpClient(LLM_BASE_URL, Duration.ofSeconds(90), "local", false, new DefaultStreamResponseParser(), 50L);
            ChatServices_LowLevel lowLevelChatService = new ChatServices_LowLevel(client, MODEL_NAME);

            TokenCountingMemory memory = new TokenCountingMemory(MAX_MEMORY_TOKENS);

            PromptTemplate promptTemplate = new PromptTemplate("You are a helpful, friendly AI assistant.", "User: {user_message}\nAI:");

            ChatServices chatService = new ChatServices(
                    lowLevelChatService,
                    memory,
                    new ModelParams.Builder().setTemperature(0.7).setMaxTokens(256).setTopP(0.9).build(),
                    promptTemplate
            );

            System.out.println("Token-Based Chatbot started. Type 'exit' to quit.");
            System.out.println("Max memory tokens: " + MAX_MEMORY_TOKENS);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nYou: ");
                String userMessage = scanner.nextLine();

                if ("exit".equalsIgnoreCase(userMessage)) {
                    break;
                }

                // For this example, we'll just add and let TokenCountingMemory handle eviction.
                Message userMsg = new Message(MessageRole.USER, userMessage);
                int userTokens = TokenCalculator.estimateTokens(userMsg);
                System.out.println("User message estimated tokens: " + userTokens);

                // Check for streaming command
                boolean useStreaming = userMessage.equalsIgnoreCase("/stream");
                if (useStreaming) {
                    System.out.print("AI (streaming): ");
                    StringBuilder streamedResponse = new StringBuilder();
                    chatService.chatStream(userMessage.replace("/stream", "").trim(), new StreamHandler() {
                        @Override
                        public void onStream(com.aiforjava.llm.streams.StreamResponse response) {
                            if (response.getContent() != null) {
                                System.out.print(response.getContent());
                                streamedResponse.append(response.getContent());
                            }
                        }
                    });
                    System.out.println(); // New line after streaming completes

                    // For streaming, we estimate tokens for the AI response
                    Message assistantMessage = new Message(MessageRole.ASSISTANT, streamedResponse.toString());
                    int aiEstimatedTokens = TokenCalculator.estimateTokens(assistantMessage);
                    System.out.println("AI response estimated tokens: " + aiEstimatedTokens);

                } else {
                    LLMResponse llmResponse = chatService.chatAndGetTokens(userMessage);
                    System.out.println("AI: " + llmResponse.getContent());
                    System.out.println("AI response actual tokens: " + llmResponse.getTotalTokens());
                }
                System.out.println("Current memory tokens: " + memory.getCurrentTokens());
            }

            scanner.close();
            System.out.println("Chatbot exited.");

        } catch (LLMServiceException e) {
            System.err.println("LLM Service Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}