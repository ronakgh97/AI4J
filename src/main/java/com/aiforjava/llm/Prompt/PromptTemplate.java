package com.aiforjava.llm.Prompt;

/**
 * A template class for structuring prompts for Large Language Models (LLMs).
 * This class separates the system prompt from the user message template,
 * allowing for clear role-based prompting.
 */
public class PromptTemplate {
    private final String systemPrompt;
    private final String userMessageTemplate;

    /**
     * Constructs a new PromptTemplate.
     *
     * @param systemPrompt The initial system instruction or persona for the LLM.
     * @param userMessageTemplate The template string for user messages,
     *                            which should contain a "{user_message}" placeholder.
     */
    public PromptTemplate(String systemPrompt, String userMessageTemplate) {
        this.systemPrompt = systemPrompt;
        this.userMessageTemplate = userMessageTemplate;
    }

    /**
     * Returns the system prompt.
     *
     * @return The system prompt string.
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    /**
     * Formats a user message according to the user message template.
     *
     * @param userMessage The raw user message.
     * @return The formatted user message string.
     */
    public String formatUserMessage(String userMessage) {
        return userMessageTemplate.replace("{user_message}", userMessage);
    }
}
