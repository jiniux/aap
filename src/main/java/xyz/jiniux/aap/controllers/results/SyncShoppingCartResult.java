package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;
import java.util.List;

public record SyncShoppingCartResult(
    List<SyncShoppingCartResultItem> items,
    List<SyncShoppingCartResultRemovedItem> removedItems,
    List<SyncShoppingCartResultPriceChangedItem> priceChangedItems
) implements Serializable {}
