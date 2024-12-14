package xyz.jiniux.aap.domain.order.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ItemsPriceChangedWhilePlacingOrderException extends Exception {
    public record Info(String bookIsbn, StockFormat stockFormat, StockQuality stockQuality, BigDecimal oldPrice, BigDecimal newPrice) {}

    public ItemsPriceChangedWhilePlacingOrderException(List<Info> info) {
        super("price of several items changed during checkout");
        this.info = List.copyOf(info);
    }

    private final List<Info> info;
}
