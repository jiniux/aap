package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.hibernate.validator.constraints.ISBN;
import xyz.jiniux.aap.validation.ValidList;
import xyz.jiniux.aap.validation.ValidStockFormat;
import xyz.jiniux.aap.validation.ValidStockQuality;

import java.util.List;

public record SyncShoppingCartRequest(
    @Valid
    ValidList<SyncShoppingCartRequestItem> items,

    @Min(0)
    @NotNull
    long version
) {}
