package xyz.jiniux.aap.domain.catalog.requests;


import xyz.jiniux.aap.validation.ValidPublisherName;

import java.io.Serializable;

public record EditPublisherRequest(
    @ValidPublisherName
    String name
) implements Serializable {}
