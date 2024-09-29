package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;
import java.util.List;

public record BookSearchResultEntry(
    String title,
    String isbn,
    List<EntryAuthor> authors,
    EntryPublisher publisher,
    int publicationYear,
    String edition
) implements Serializable {
    public record EntryAuthor(
        String id,
        String firstName,
        String lastName
    ) implements Serializable { }

    public record EntryPublisher(
        String id,
        String name
    ) implements Serializable { }
}
