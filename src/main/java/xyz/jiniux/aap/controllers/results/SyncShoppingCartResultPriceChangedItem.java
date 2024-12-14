package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public record SyncShoppingCartResultPriceChangedItem(
    String isbn,
    String stockFormat,
    String stockQuality,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal oldPriceEur,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal newPriceEur
) implements Serializable {}
