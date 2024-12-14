package xyz.jiniux.aap.domain.warehouse.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Getter
public class StockNotOnSaleException extends Exception {
    public StockNotOnSaleException(String isbn, StockFormat stockFormat, StockQuality stockQuality) {
      super("The stock of book " + isbn + "(stockFormat: " + stockFormat + ", stockQuality: " + stockFormat + ") is already not on sale");
      this.isbn = isbn;
      this.stockFormat = stockFormat;
      this.stockQuality = stockQuality;
    }

    private final String isbn;
    private final StockFormat stockFormat;
    private final StockQuality stockQuality;
}