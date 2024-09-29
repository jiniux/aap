package xyz.jiniux.aap.controllers.requests;


import jakarta.validation.constraints.NotNull;
import xyz.jiniux.aap.validation.ValidAuthorFirstName;
import xyz.jiniux.aap.validation.ValidAuthorLastName;

import java.io.Serializable;

public record AuthorRegistrationRequest (
    @NotNull
    @ValidAuthorFirstName
    String firstName,

    @NotNull
    @ValidAuthorLastName
    String lastName
) implements Serializable {}
