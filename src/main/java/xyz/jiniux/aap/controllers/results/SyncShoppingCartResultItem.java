package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;

public record SyncShoppingCartResultItem(
    String isbn,
    String stockFormat,
    String stockQuality,
    long quantity,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal priceEur
) implements Serializable { }
