package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record StockResultElement(
    String isbn,
    String format,
    String quality,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal priceEur
) {}
