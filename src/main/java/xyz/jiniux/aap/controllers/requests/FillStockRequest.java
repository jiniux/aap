package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.Min;

import java.io.Serializable;

public record FillStockRequest(
    @Min(1)
    int quantity
) implements Serializable { }