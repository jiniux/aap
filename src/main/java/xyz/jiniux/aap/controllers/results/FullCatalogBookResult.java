package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;
import java.util.List;

public record FullCatalogBookResult(
    String title,
    String isbn,
    String description,
    int publicationYear,
    String edition,
    List<Author> authors,
    Publisher publisher
) implements Serializable {
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