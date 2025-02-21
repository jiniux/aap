package xyz.jiniux.aap.controllers.requests;

import jakarta.validation.Valid;

public record UpdateItemShoppingCartRequest(
    @Valid
    UpdateItemShoppingCartRequestItem item
) {}
