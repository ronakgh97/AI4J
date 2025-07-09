package com.aiforjava.llm;

import com.aiforjava.exception.LLMParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of {@link StreamResponseParser} for parsing common LLM streaming JSON responses.
 * This parser expects a format similar to OpenAI's streaming API, where each line starts with "data: "
 * and contains a JSON object with a "choices" array, and a "delta" object with "content".
 */
public class DefaultStreamResponseParser implements StreamResponseParser {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicBoolean isFirstChunk = new AtomicBoolean(true);

    @Override
    public String parse(String line) throws LLMParseException {
        if (line.startsWith("data: ")) {
            String jsonData = line.substring(6).trim();
            if (jsonData.equals("[DONE]")) {
                return null; // Signal end of stream
            }
            try {
                JsonNode rootNode = mapper.readTree(jsonData);
                JsonNode choices = rootNode.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    JsonNode contentNode = choices.get(0).path("delta").path("content");
                    if (!contentNode.isMissingNode()) {
                        String content = contentNode.asText();
                        if (isFirstChunk.getAndSet(false) && content != null) {
                            content = content.stripLeading();
                        }
                        return content;
                    }
                }
            } catch (IOException e) {
                throw new LLMParseException("Failed to parse streaming LLM response JSON: " + e.getMessage(), e);
            }
        }
        return null; // Ignore lines that don't start with "data: " or don't contain content
    }
}