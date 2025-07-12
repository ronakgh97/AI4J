package com.aiforjava.llm.streams;

/**
 * A functional interface for handling streamed LLM responses on a word-by-word basis.
 */
@FunctionalInterface
public interface WordStreamHandler {
    /**
     * Called when a complete word is received from the LLM stream.
     *
     * @param word The complete word received from the stream.
     */
    void onWord(String word);
}
