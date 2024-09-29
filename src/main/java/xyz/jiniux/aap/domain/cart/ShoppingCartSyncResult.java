package xyz.jiniux.aap.domain.cart;

import xyz.jiniux.aap.domain.model.ShoppingCart;

import java.util.List;

public record ShoppingCartSyncResult(ShoppingCart shoppingCart, List<RemovedShoppingCartItem> removedItems) {}
