package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.Min;

import java.io.Serializable;
import java.math.BigDecimal;

public record SetStockPriceRequest(
    @Min(0)
    BigDecimal priceEur
) implements Serializable {}
