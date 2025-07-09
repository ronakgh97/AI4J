package com.aiforjava.llm;

/**
 * ModelParams is a data class that encapsulates various parameters used to control the behavior
 * of Large Language Models (LLMs) during text generation. These parameters influence aspects
 * like the creativity, length, and diversity of the generated responses.
 * It uses a Builder pattern for easy and readable construction of parameter sets.
 */
public class ModelParams {
    // Controls the randomness of the output. Higher values (e.g., 0.8) make the output more random,
    // while lower values (e.g., 0.2) make it more focused and deterministic.
    private double temperature = 0.7;
    // The maximum number of tokens (words or sub-word units) the LLM should generate in its response.
    private int maxTokens = 512;
    // Controls diversity via nucleus sampling. The model considers tokens whose cumulative probability
    // exceeds `topP`. Lower values (e.g., 0.1) result in more focused output.
    private double topP = 0.9;
    // Penalizes new tokens based on their existing frequency in the text so far.
    // Higher values decrease the likelihood of the model repeating the same lines verbatim.
    private double frequencyPenalty = 0.0;
    // Penalizes new tokens based on whether they appear in the text so far.
    // Higher values increase the model's likelihood to talk about new topics.
    private double presencePenalty = 0.0;
    // A flag indicating whether the response should be streamed (true) or returned as a single block (false).
    private boolean stream = false;

    /**
     * Builder class for constructing ModelParams instances.
     * Provides a fluent API for setting model parameters.
     */
    public static class Builder {
        private final ModelParams params = new ModelParams();

        /**
         * Sets the temperature for the model.
         * @param value The temperature value.
         * @return The Builder instance.
         */
        public Builder setTemperature(double value) {
            if (value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException("Temperature must be between 0.0 and 1.0");
            }
            params.temperature = value;
            return this;
        }

        /**
         * Sets the maximum number of tokens for the model's response.
         * @param value The maximum tokens value.
         * @return The Builder instance.
         */
        public Builder setMaxTokens(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Max tokens must be greater than 0");
            }
            params.maxTokens = value;
            return this;
        }

        /**
         * Sets the top-p value for nucleus sampling.
         * @param value The top-p value.
         * @return The Builder instance.
         */
        public Builder setTopP(double value) {
            if (value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException("TopP must be between 0.0 and 1.0");
            }
            params.topP = value;
            return this;
        }

        /**
         * Sets the frequency penalty for the model.
         * @param value The frequency penalty value.
         * @return The Builder instance.
         */
        public Builder setFrequencyPenalty(double value) {
            if (value < -2.0 || value > 2.0) {
                throw new IllegalArgumentException("Frequency penalty must be between -2.0 and 2.0");
            }
            params.frequencyPenalty = value;
            return this;
        }

        /**
         * Sets the presence penalty for the model.
         * @param value The presence penalty value.
         * @return The Builder instance.
         */
        public Builder setPresencePenalty(double value) {
            if (value < -2.0 || value > 2.0) {
                throw new IllegalArgumentException("Presence penalty must be between -2.0 and 2.0");
            }
            params.presencePenalty = value;
            return this;
        }

        /**
         * Sets whether the response should be streamed.
         * @param value True for streaming, false otherwise.
         * @return The Builder instance.
         */
        public Builder setStream(boolean value) {
            params.stream = value;
            return this;
        }

        /**
         * Builds and returns a new ModelParams instance with the configured parameters.
         * @return A new ModelParams object.
         */
        public ModelParams build() {
            return params;
        }
    }

    /**
     * Returns the temperature setting.
     * @return The temperature value.
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Returns the maximum tokens setting.
     * @return The maximum tokens value.
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Returns the top-p setting.
     * @return The top-p value.
     */
    public double getTopP() {
        return topP;
    }

    /**
     * Returns the frequency penalty setting.
     * @return The frequency penalty value.
     */
    public double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    /**
     * Returns the presence penalty setting.
     * @return The presence penalty value.
     */
    public double getPresencePenalty() {
        return presencePenalty;
    }

    /**
     * Returns whether streaming is enabled.
     * @return True if streaming is enabled, false otherwise.
     */
    public boolean isStream() {
        return stream;
    }
}
