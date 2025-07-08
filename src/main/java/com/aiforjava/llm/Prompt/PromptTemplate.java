package com.aiforjava.llm.Prompt;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple template engine for creating prompts with variables.
 * This class allows you to define a prompt with placeholders (e.g., "{{name}}")
 * and then substitute them with actual values.
 */
public class PromptTemplate {
    private final String template;
    private final Map<String, String> variables = new HashMap<>();

    /**
     * Constructs a new PromptTemplate with the given template string.
     *
     * @param template The template string with placeholders.
     */
    public PromptTemplate(String template) {
        this.template = template;
    }

    /**
     * Sets a variable to be used in the template.
     *
     * @param key The name of the variable (without the curly braces).
     * @param value The value to substitute for the variable.
     * @return This PromptTemplate instance for method chaining.
     */
    public PromptTemplate set(String key, String value) {
        variables.put(key, value);
        return this;
    }

    /**
     * Builds the final prompt string by substituting all the variables.
     *
     * @return The final prompt string with all variables replaced.
     */
    public String build() {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
