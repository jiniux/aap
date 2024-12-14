package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

public record SetStockPriceRequest(
    @Min(0)
    @NotNull
    BigDecimal priceEur
) implements Serializable {}
