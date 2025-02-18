package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;

public record FullPublisherResult(
    String id,
    String name
) implements Serializable { }
