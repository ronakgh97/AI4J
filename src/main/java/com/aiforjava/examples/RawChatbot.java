package com.aiforjava.examples;

import com.aiforjava.exception.ExceptionHandler;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.client.LLM_Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.Scanner;

/**
 * This example demonstrates the lowest level of interaction with the LLM using AI4J.
 * It showcases how to manually construct the full JSON request payload and send it
 * directly to the LLM endpoint using the `generateRaw` method of `ChatServices_LowLevel`.
 * This provides maximum flexibility for custom API interactions or when dealing with
 * LLMs that have non-standard request formats.
 */
public class RawChatbot {

    public static void main(String[] args) {
        LLM_Client client = new DefaultHttpClient("http://localhost:1234", Duration.ofSeconds(90),"local", false, new DefaultStreamResponseParser(), 50L);
        ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");
        ObjectMapper mapper = new ObjectMapper();

        Scanner scanner = new Scanner(System.in);
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║             RAW CHATBOT - FULL CONTROL            ║");
        System.out.println("║              Type 'exit' to quit                  ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");

        while (true) {
            System.out.print("\nYou:-> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            ObjectNode request = mapper.createObjectNode();
            request.put("model", "gemma-3-4b-it");
            request.put("temperature", 0.7);
            request.put("max_tokens", 256);
            request.put("stream", false);

            ArrayNode messagesNode = request.putArray("messages");
            messagesNode.add(mapper.createObjectNode()
                    .put("role", "system")
                    .put("content", "You are a helpful assistant. Respond concisely."));
            messagesNode.add(mapper.createObjectNode()
                    .put("role", "user")
                    .put("content", input));

            String requestJson = request.toString();

            try {
                System.out.print("LLM: ");
                String rawResponse = llm.generateRaw("v1/chat/completions", requestJson);
                String content = mapper.readTree(rawResponse)
                                       .path("choices").get(0)
                                       .path("message").path("content").asText();
                System.out.println(content);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
        scanner.close();
    }
}
