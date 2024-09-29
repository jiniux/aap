package xyz.jiniux.aap.domain.catalog;

import lombok.Builder;
import xyz.jiniux.aap.validation.ValidAuthorFirstName;
import xyz.jiniux.aap.validation.ValidAuthorLastName;

import java.io.Serializable;

@Builder
public record PartialAuthor(
    @ValidAuthorFirstName
    String firstName,
    @ValidAuthorLastName
    String lastName
) implements Serializable {}
