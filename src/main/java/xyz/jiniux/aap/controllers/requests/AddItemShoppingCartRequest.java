package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemShoppingCartRequest(
    @Valid
    AddItemShoppingCartRequestItem item
) {}
