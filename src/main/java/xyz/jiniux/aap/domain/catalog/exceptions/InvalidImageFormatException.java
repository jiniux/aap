package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidImageFormatException extends Exception {
    public InvalidImageFormatException(List<String> expectedFormats, String format) {
        super("Invalid image format. Expected one of " + String.join(", ", expectedFormats) + " but got " + format);

        this.expectedFormats = List.copyOf(expectedFormats);
        this.format = format;
    }

    private final List<String> expectedFormats;
    private final String format;
}
