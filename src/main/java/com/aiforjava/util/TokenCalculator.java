package com.aiforjava.util;

import com.aiforjava.message.Message;
import com.aiforjava.message.MessagePart;
import com.aiforjava.message.files.TextPart;
import com.aiforjava.message.files.ImagePart;

import java.util.List;

/**
 * Utility class for estimating token counts of text and messages.
 * This provides a simplified estimation and is not a true LLM tokenizer.
 */
public class TokenCalculator {

    // Arbitrary token cost for an image. Actual cost varies by model and image size.
    private static final int IMAGE_TOKEN_COST = 100;

    /**
     * Estimates the token count of a given string.
     * This is a simplified word count based on whitespace.
     * @param text The text to estimate tokens for.
     * @return The estimated token count.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        // Simple word count based on whitespace
        return text.trim().split("\\s+").length;
    }

    /**
     * Estimates the token count of a single Message object.
     * @param message The Message object to estimate tokens for.
     * @return The estimated token count.
     */
    public static int estimateTokens(Message message) {
        int tokens = 0;
        for (MessagePart part : message.getContentParts()) {
            if (part instanceof TextPart) {
                tokens += estimateTokens(((TextPart) part).getText());
            } else if (part instanceof ImagePart) {
                tokens += IMAGE_TOKEN_COST;
            }
        }
        // Account for role and other message overhead (e.g., 4 tokens per message for OpenAI chat format)
        // This is a very rough heuristic.
        tokens += 4; 
        return tokens;
    }

    /**
     * Estimates the total token count for a list of Message objects.
     * @param messages The list of Message objects to estimate tokens for.
     * @return The total estimated token count.
     */
    public static int estimateTokens(List<Message> messages) {
        int totalTokens = 0;
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        for (Message message : messages) {
            totalTokens += estimateTokens(message);
        }
        return totalTokens;
    }
}
