package xyz.jiniux.aap.domain.warehouse.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Getter
public class StockPriceNotSetException extends Exception {
    public StockPriceNotSetException(String bookIsbn, StockFormat stockFormat, StockQuality stockQuality) {
        super("the price for the stock with ISBN " + bookIsbn + " and format " + stockFormat + " and quality " + stockQuality + " is not set");
        this.bookIsbn = bookIsbn;
        this.stockFormat = stockFormat;
        this.stockQuality = stockQuality;
    }

    private final String bookIsbn;
    private final StockFormat stockFormat;
    private final StockQuality stockQuality;
}
