package xyz.jiniux.aap.domain.catalog.results;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record AuthorSearchResult(List<Entry> entries) implements Serializable {
    public record Entry(
        String id, String firstName, String lastName
    ) implements Serializable { }

    public AuthorSearchResult(@NotNull List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }
}
