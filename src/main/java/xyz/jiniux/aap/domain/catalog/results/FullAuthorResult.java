package xyz.jiniux.aap.domain.catalog.results;

import java.io.Serializable;

public record FullAuthorResult(
    String id,
    String firstName,
    String lastName
) implements Serializable { }
