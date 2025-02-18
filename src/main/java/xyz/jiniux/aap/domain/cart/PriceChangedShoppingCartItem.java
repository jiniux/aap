package xyz.jiniux.aap.domain.cart;

import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.math.BigDecimal;

public record PriceChangedShoppingCartItem(
    String isbn,
    StockFormat stockFormat,
    StockQuality stockQuality,
    BigDecimal newPriceEur
) {}
