package xyz.jiniux.aap.domain.billing.events;

import xyz.jiniux.aap.domain.billing.PaymentStrategy;

import java.math.BigDecimal;

public record PaymentFailedEvent(long paymentId, BigDecimal amount, PaymentStrategy paymentStrategy) {}
