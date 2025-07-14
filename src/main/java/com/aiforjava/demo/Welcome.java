package com.aiforjava.demo;

import com.aiforjava.exception.LLMServiceException;
import com.aiforjava.llm.Chat.LowLevel.ChatServices_LowLevel;
import com.aiforjava.llm.client.DefaultHttpClient;
import com.aiforjava.llm.streams.DefaultStreamResponseParser;
import com.aiforjava.llm.client.LLM_Client;
import com.aiforjava.llm.models.ModelParams;
import com.aiforjava.message.Message;
import com.aiforjava.message.MessageRole;

import java.time.Duration;
import java.util.List;

public class Welcome {

    public static String generateWelcomeMessage() throws LLMServiceException {
        try{
            LLM_Client client = new DefaultHttpClient("http://modest-literally-fish.ngrok-free.app", Duration.ofSeconds(90),"local", false, new DefaultStreamResponseParser(), 50L);
            ChatServices_LowLevel llm = new ChatServices_LowLevel(client, "google/gemma-3-1b");

            ModelParams params = new ModelParams.Builder()
                    .setTemperature(.8)
                    .setMaxTokens(128)
                    .setTopP(0.95)
                    .build();

            String systemPrompt =
                    "Your Task is generate a Welcome Message for User," +
                            "Start with Welcome to AI4J Demo Chatbot...";

            String userPrompt =
                    "Welcome the User in warm, friendly way, Dont show options, only the message";

            List<Message> messageList = List.of(
                    new Message(MessageRole.SYSTEM, systemPrompt),
                    new Message(MessageRole.USER, userPrompt));

            return llm.generate(messageList, params).getContent();

        }catch (LLMServiceException e){
            return "Welcome to JavaSwing ChatBot";
        }
    }
}
