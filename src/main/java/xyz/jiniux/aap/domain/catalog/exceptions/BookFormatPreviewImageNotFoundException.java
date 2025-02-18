package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class BookFormatPreviewImageNotFoundException extends Exception {
    public BookFormatPreviewImageNotFoundException(long bookFormatPreviewImageId) {
        super("the book format preview image with id " + bookFormatPreviewImageId + " was not found");
        this.bookFormatPreviewImageId = bookFormatPreviewImageId;
    }

    private final long bookFormatPreviewImageId;
}
