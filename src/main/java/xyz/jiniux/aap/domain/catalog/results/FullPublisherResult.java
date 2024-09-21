package xyz.jiniux.aap.domain.catalog.results;

import java.io.Serializable;
import java.util.List;

public record FullPublisherResult(
    String id,
    String name
) implements Serializable { }
