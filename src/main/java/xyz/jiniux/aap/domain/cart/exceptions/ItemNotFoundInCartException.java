package xyz.jiniux.aap.domain.cart.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Getter
public class ItemNotFoundInCartException extends Exception {
    public ItemNotFoundInCartException(String isbn, StockFormat stockFormat, StockQuality stockQuality) {
        super("the following item was not found in the cart: " +
            String.join(",", isbn, stockFormat.toString(), stockQuality.toString()));

        this.isbn = isbn;
        this.stockFormat = stockFormat;
        this.stockQuality = stockQuality;
    }

    private final String isbn;
    private final StockFormat stockFormat;
    private final StockQuality stockQuality;
}
