package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import xyz.jiniux.aap.validation.ValidSet;

import java.math.BigDecimal;

public record CheckoutRequest(
    @NotNull
    @Valid
    ValidSet<CheckoutRequestCartItem> items,

    @NotNull
    @Valid
    CheckoutRequestPaymentStrategy paymentStrategy,

    @NotNull
    @Valid
    CheckoutRequestAddress address,

    @Min(0)
    @NotNull
    BigDecimal shipmentCost,

    long cartVersion
) {}
