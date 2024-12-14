package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.hibernate.validator.constraints.ISBN;
import xyz.jiniux.aap.validation.ValidStockFormat;
import xyz.jiniux.aap.validation.ValidStockQuality;

import java.math.BigDecimal;

public record CheckoutRequestCartItem(
    @NonNull
    @ISBN
    String isbn,

    @NonNull
    @ValidStockFormat
    String stockFormat,

    @NonNull
    @ValidStockQuality
    String stockQuality,

    @Min(1)
    @NotNull
    long quantity,

    @Min(0)
    @NotNull
    BigDecimal priceEur
) {}
