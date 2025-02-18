package xyz.jiniux.aap.domain.billing.events;

import xyz.jiniux.aap.domain.billing.PaymentStrategy;
import java.math.BigDecimal;

public record PaymentInitiatedEvent(long paymentId, BigDecimal amount, PaymentStrategy paymentStrategy) {}
