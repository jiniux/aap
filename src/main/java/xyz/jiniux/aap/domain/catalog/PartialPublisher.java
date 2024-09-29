package xyz.jiniux.aap.domain.catalog;

import lombok.Builder;
import xyz.jiniux.aap.validation.ValidAuthorFirstName;
import xyz.jiniux.aap.validation.ValidAuthorLastName;
import xyz.jiniux.aap.validation.ValidPublisherName;

import java.io.Serializable;

@Builder
public record PartialPublisher(
    @ValidPublisherName
    String name
) implements Serializable {}
