package com.aiforjava.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Utility class for encoding images to Base64 strings.
 */
public class ImageEncoder {

    /**
     * Encodes an image file into a Base64 string.
     *
     * @param imageFile The image file to encode.
     * @return The Base64 encoded string of the image.
     * @throws IOException If an I/O error occurs reading the file.
     */
    public static String encodeImageToBase64(File imageFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
