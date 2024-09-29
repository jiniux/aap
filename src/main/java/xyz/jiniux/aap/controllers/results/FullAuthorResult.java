package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;

public record FullAuthorResult(
    String id,
    String firstName,
    String lastName
) implements Serializable { }
