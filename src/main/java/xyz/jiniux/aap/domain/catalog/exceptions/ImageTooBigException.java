package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class ImageTooBigException extends Exception {
    public ImageTooBigException(long maxSize, long actualSize) {
        super("Image is too big. Max size is " + maxSize + " but got " + actualSize);

        this.maxSize = maxSize;
        this.actualSize = actualSize;
    }

    private final long maxSize;
    private final long actualSize;
}
