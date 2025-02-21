package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummariesResult(
    List<OrderSummary> orders
) {
    public record OrderSummary(
        String id,
        String state,
        String placeAt,
        long itemCount,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal totalEur
    ) {}

}
