package xyz.jiniux.aap.domain.cart;

import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

public record RemovedShoppingCartItem(
    String isbn,
    StockFormat stockFormat,
    StockQuality stockQuality
) {}
