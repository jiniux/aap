package xyz.jiniux.aap.domain.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

import java.io.Serializable;

@Embeddable
public record Address(
    String country,
    String state,
    String city,
    String street,
    String number,
    String zipCode,
    String recipientName
) implements Serializable {}
