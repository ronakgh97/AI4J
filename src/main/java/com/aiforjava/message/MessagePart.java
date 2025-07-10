package com.aiforjava.message;

import com.aiforjava.message.files.ImagePart;
import com.aiforjava.message.files.TextPart;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a part of a multimodal message content. This interface allows for different types
 * of content (e.g., text, image) to be included within a single message.
 * Uses Jackson annotations for polymorphic deserialization.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPart.class, name = "text"),
        @JsonSubTypes.Type(value = ImagePart.class, name = "image_url")
})
public interface MessagePart {
    // No methods needed here, as type is handled by JsonTypeInfo and content by concrete classes
}
