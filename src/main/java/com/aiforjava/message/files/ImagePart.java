package com.aiforjava.message.files;

import com.aiforjava.message.MessagePart;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an image part of a multimodal message.
 * The image data is expected to be in Base64 format.
 */
public class ImagePart implements MessagePart {

    private final ImageUrl image_url;

    @JsonCreator
    public ImagePart(@JsonProperty("image_url") ImageUrl imageUrl) {
        if (imageUrl == null || imageUrl.getUrl() == null || imageUrl.getUrl().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty");
        }
        this.image_url = imageUrl;
    }

    /**
     * Convenience constructor for creating an ImagePart from a Base64 string.
     * Automatically formats the Base64 data into the required 'data:image/jpeg;base64,...' format.
     * Assumes JPEG format for simplicity, but could be extended to detect image type.
     *
     * @param base64Data The raw Base64 encoded image data.
     */
    public ImagePart(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            throw new IllegalArgumentException("Base64 data cannot be null or empty");
        }
        // Assuming JPEG for now, but could be made dynamic
        this.image_url = new ImageUrl("data:image/jpeg;base64," + base64Data);
    }

    public ImageUrl getImage_url() {
        return image_url;
    }

    /**
     * Inner class to represent the nested 'image_url' object in the JSON structure.
     */
    public static class ImageUrl {
        private final String url;

        @JsonCreator
        public ImageUrl(@JsonProperty("url") String url) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL cannot be null or empty");
            }
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }
}
