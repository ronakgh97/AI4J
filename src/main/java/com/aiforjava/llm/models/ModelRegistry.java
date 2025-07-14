package com.aiforjava.llm.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A registry for managing LLM model capabilities. This class allows for centralizing
 * information about which features (e.g., text, vision, thinking) each model supports.
 * It is designed to be a general-purpose, reusable, flexible, and scalable component.
 */
public class ModelRegistry {

    private static final HashMap<String, Set<ModelFeature>> modelCapabilities = new HashMap<>();

    // Static initializer to register default models and their capabilities
    static {
        // Example: Register a text-only model
        registerModel("google/gemma-3-1b", Set.of(ModelFeature.TEXT));

        // Example: Register a multimodal model with thinking capability
        //registerModel("qwen/qwen3-4b", Set.of(ModelFeature.TEXT, ModelFeature.THINK));

        // Example: Register a multimodal model with vision capability
        //registerModel("google/gemma-3-4b", Set.of(ModelFeature.TEXT, ModelFeature.VISION));

        // Add more models and their capabilities as needed
    }

    /**
     * Registers a model with its supported features.
     *
     * @param modelName The unique name of the model.
     * @param features A set of ModelFeature enums representing the capabilities of the model.
     */
    public static void registerModel(String modelName, Set<ModelFeature> features) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty.");
        }
        if (features == null || features.isEmpty()) {
            // A model should at least support TEXT, or have some defined features
            System.err.println("Warning: Registering model " + modelName + " with no features. Defaulting to TEXT.");
            modelCapabilities.put(modelName, Collections.unmodifiableSet(Set.of(ModelFeature.TEXT)));
        } else {
            modelCapabilities.put(modelName, Collections.unmodifiableSet(new HashSet<>(features)));
        }
    }

    /**
     * Retrieves the capabilities for a given model.
     *
     * @param modelName The name of the model.
     * @return An unmodifiable set of ModelFeature enums supported by the model, or an empty set if the model is not registered.
     */
    public static Set<ModelFeature> getCapabilities(String modelName) {
        return Collections.unmodifiableSet(modelCapabilities.getOrDefault(modelName, Collections.emptySet()));
    }

    /**
     * Returns a set of all registered model names.
     *
     * @return An unmodifiable set of all registered model names.
     */
    public static Set<String> getAllModelNames() {
        return Collections.unmodifiableSet(modelCapabilities.keySet());
    }

    /**
     * Checks if a specific model supports a given feature.
     *
     * @param modelName The name of the model.
     * @param feature The feature to check for.
     * @return True if the model supports the feature, false otherwise.
     */
    public static boolean supportsFeature(String modelName, ModelFeature feature) {
        return getCapabilities(modelName).contains(feature);
    }
}
