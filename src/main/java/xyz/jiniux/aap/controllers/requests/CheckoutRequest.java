package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import xyz.jiniux.aap.validation.ValidList;
import xyz.jiniux.aap.validation.ValidSet;

public record CheckoutRequest(
    @NotNull
    @Valid
    ValidSet<CheckoutRequestCartItem> items,

    @NotNull
    @Valid
    CheckoutRequestPaymentStrategy paymentStrategy,

    @NotNull
    @Valid
    CheckoutRequestAddress address
) {}
