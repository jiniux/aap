package xyz.jiniux.aap.domain.catalog.results;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record BookSearchResult(List<Entry> entries) implements Serializable {
    public record EntryAuthor(
        String id,
        String firstName,
        String lastName
    ) implements Serializable { }

    public record EntryPublisher(
        String id,
        String name
    ) implements Serializable { }

    public record Entry(
        String title,
        String isbn,
        List<EntryAuthor> authors,
        EntryPublisher publisher,
        int publicationYear,
        String edition
    ) implements Serializable { }

    public BookSearchResult(@NotNull List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }
}
