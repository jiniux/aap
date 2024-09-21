package xyz.jiniux.aap.domain.catalog.results;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record PublisherSearchResult(List<Entry> entries) implements Serializable {
    public record Entry(
        String id, String name
    ) implements Serializable { }

    public PublisherSearchResult(@NotNull List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }
}
