package xyz.jiniux.aap.domain.warehouse.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Getter
public class UnsupportedStockQualityException extends Exception {
    public UnsupportedStockQualityException(String bookIsbn, StockFormat stockFormat, StockQuality stockQuality) {
        super("Unsupported stock quality-format pair (" + stockFormat + ", " + stockQuality + ")");
        this.bookIsbn = bookIsbn;
        this.stockFormat = stockFormat;
        this.stockQuality = stockQuality;
    }

    private final String bookIsbn;
    private final StockFormat stockFormat;
    private final StockQuality stockQuality;
}
