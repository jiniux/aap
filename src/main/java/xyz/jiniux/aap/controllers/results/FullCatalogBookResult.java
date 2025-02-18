package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;


public record FullCatalogBookResult(
    String title,
    String isbn,
    String description,
    int publicationYear,
    String edition,
    List<Author> authors,
    Publisher publisher,
    List<Stock> stocks,
    List<FormatPreviewImages> formatPreviewImages
) implements Serializable {
    public record FormatPreviewImages(
        String url,
        String format
    ) implements Serializable { }

    public record Stock (
        String format,
        String quality,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal priceEur
    ) implements Serializable { }

    public record Author(
        String id,
        String firstName,
        String lastName
    ) implements Serializable { }

    public record Publisher(
        String id,
        String name
    ) implements Serializable { }
}