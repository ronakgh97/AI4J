package com.aiforjava.llm.streams;

import com.aiforjava.exception.LLMParseException;

/**
 * Defines the contract for parsing a single line of a streaming LLM response.
 * Implementations of this interface are responsible for extracting meaningful content
 * from raw streaming data, typically JSON chunks.
 */
public interface StreamResponseParser {

    /**
     * Parses a single line of a streaming LLM response.
     *
     * @param line The raw string line received from the LLM stream.
     * @return The extracted content as a String, or null if the line does not contain content (e.g., a [DONE] signal).
     * @throws LLMParseException If the line cannot be parsed or does not conform to the expected format.
     */
    String parse(String line) throws LLMParseException;
}