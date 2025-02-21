package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.NotBlank;

public record ShipmentCostRequest(
    @NotBlank
    String country,
    @NotBlank
    String state,
    @NotBlank
    String city,
    @NotBlank
    String street,
    @NotBlank
    String zipCode
) {}
