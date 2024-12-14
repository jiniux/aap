package xyz.jiniux.aap.domain.warehouse.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Getter
public class NotEnoughItemsInStockException extends Exception {
    public NotEnoughItemsInStockException(String isbn, StockFormat stockFormat, StockQuality stockQuality, long quantity) {
        super("Cannot satisfy the demand of " + quantity + "items for stock of book " + isbn + "(stockFormat: " + stockFormat + ", stockQuality: " + stockFormat + ")");
        this.isbn = isbn;
        this.stockFormat = stockFormat;
        this.stockQuality = stockQuality;
        this.quantity = quantity;
    }

    private final long quantity;
    private final String isbn;
    private final StockFormat stockFormat;
    private final StockQuality stockQuality;
}