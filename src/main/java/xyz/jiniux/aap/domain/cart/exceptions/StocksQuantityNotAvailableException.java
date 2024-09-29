package xyz.jiniux.aap.domain.cart.exceptions;

import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;

@Getter
public class StocksQuantityNotAvailableException extends Exception {
    public StocksQuantityNotAvailableException(List<ImmutableTriple<String, StockFormat, StockQuality>> stocks) {
        super("the following stocks are no longer available: " +
            String.join(",", stocks.stream().map(Triple::toString).toString()));

        this.details = stocks.stream().toList();
    }

    private final List<ImmutableTriple<String, StockFormat, StockQuality>> details;
}
