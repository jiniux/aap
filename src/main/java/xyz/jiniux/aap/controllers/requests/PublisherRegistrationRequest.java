package xyz.jiniux.aap.controllers.requests;


import jakarta.validation.constraints.NotNull;
import xyz.jiniux.aap.validation.ValidPublisherName;

import java.io.Serializable;

public record PublisherRegistrationRequest(
    @NotNull
    @ValidPublisherName
    String name
) implements Serializable {}
