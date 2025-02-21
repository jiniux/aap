package xyz.jiniux.aap.domain.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record Address(
    String country,
    String state,
    String city,
    String street,
    String zipCode,
    String recipientName
) implements Serializable {}
