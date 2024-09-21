package xyz.jiniux.aap.domain.catalog.requests;


import xyz.jiniux.aap.validation.ValidAuthorFirstName;
import xyz.jiniux.aap.validation.ValidAuthorLastName;

import java.io.Serializable;

public record EditAuthorRequest(
    @ValidAuthorFirstName
    String firstName,
    @ValidAuthorLastName
    String lastName
) implements Serializable {}
