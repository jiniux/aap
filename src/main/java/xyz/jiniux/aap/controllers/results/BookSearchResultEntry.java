package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record BookSearchResultEntry(
    String title,
    String isbn,
    List<EntryAuthor> authors,
    EntryPublisher publisher,
    int publicationYear,
    String edition,
    List<EntryStock> stocks,
    List<EntryFormatPreviewImages> formatPreviewImages
) implements Serializable {
    public record EntryAuthor(
        String id,
        String firstName,
        String lastName,
        boolean hasPicture
    ) implements Serializable { }

    public record EntryStock(
        String format,
        String quality,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal priceEur
    ) implements Serializable { }

    public record EntryFormatPreviewImages(
        String url,
        String format
    ) implements Serializable { }

    public record EntryPublisher(
        String id,
        String name
    ) implements Serializable { }
}
