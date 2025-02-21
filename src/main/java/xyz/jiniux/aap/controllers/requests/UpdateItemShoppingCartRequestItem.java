package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.constraints.Min;
import lombok.NonNull;
import org.hibernate.validator.constraints.ISBN;
import xyz.jiniux.aap.validation.ValidStockFormat;
import xyz.jiniux.aap.validation.ValidStockQuality;

public record UpdateItemShoppingCartRequestItem(
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
    long quantity
) {}
