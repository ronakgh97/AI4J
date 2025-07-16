
package com.aiforjava.llm.streams;

/**
 * The StreamHandler interface is a functional interface designed to process
 * chunks of content as they are received from a streaming LLM response.
 * Implementations of this interface define how each piece of streamed data should be handled,
 * for example, by printing to the console or appending to a string builder.
 */
@FunctionalInterface
public interface StreamHandler {
    /**
     * Called when a new chunk of content is received from the LLM stream.
     *
     * @param response The {@link StreamResponse} object containing the content and reasoning for the current chunk.
     */
    void onStream(StreamResponse response);
}

