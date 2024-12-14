package xyz.jiniux.aap.domain.order.events;

import xyz.jiniux.aap.domain.accounting.PaymentStrategy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderPlacedEvent (
    long orderId,
    OffsetDateTime placedAt,
    String username,
    BigDecimal finalPrice,
    PaymentStrategy paymentStrategy
) {}
