package com.aiforjava.llm.streams;

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
    public StreamResponse parse(String line) throws LLMParseException {
        if (line.startsWith("data: ")) {
            String jsonData = line.substring(6).trim();
            if (jsonData.equals("[DONE]")) {
                return null; // Signal end of stream
            }
            try {
                JsonNode rootNode = mapper.readTree(jsonData);
                JsonNode choices = rootNode.path("choices");
                if (choices.isArray() && !choices.isEmpty()) {
                    JsonNode deltaNode = choices.get(0).path("delta");
                    JsonNode contentNode = deltaNode.path("content");
                    JsonNode reasoningNode = deltaNode.path("reasoning_content");

                    String content = null;
                    if (!contentNode.isMissingNode()) {
                        content = contentNode.asText();
                        if (isFirstChunk.getAndSet(false) && content != null) {
                            content = content.stripLeading();
                        }
                    }

                    String reasoningContent = reasoningNode.isMissingNode() ? null : reasoningNode.asText();

                    if (content != null || reasoningContent != null) {
                        return new StreamResponse(content, reasoningContent);
                    }
                }
            } catch (IOException e) {
                throw new LLMParseException("Failed to parse streaming LLM response JSON: " + e.getMessage(), e);
            }
        }
        return null; // Ignore lines that don't start with "data: " or don't contain content
    }
}