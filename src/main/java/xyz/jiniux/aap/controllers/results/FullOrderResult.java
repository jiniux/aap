package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public record FullOrderResult(
    long id,
    String state,
    String placeAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal shipmentCostEur,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalEur,
    List<OrderItem> items,
    OrderAddress address,
    OrderPayment payment
) {
    public record OrderItem(
        String isbn,
        String stockFormat,
        String stockQuality,
        long quantity,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal priceEur
    ) {}

    public record OrderPayment(
        String id,
        String method,
        String state,
        Object additionalInfo
    ) {}

    public record OrderAddress(
        String country,
        String state,
        String city,
        String street,
        String zipCode,
        String recipientName
    ) {}
}
