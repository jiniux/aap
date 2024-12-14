package xyz.jiniux.aap.domain.accounting.events;

import xyz.jiniux.aap.domain.accounting.PaymentStrategy;
import xyz.jiniux.aap.domain.model.PaymentState;

import java.math.BigDecimal;

public record PaymentInitiatedEvent(long paymentId, BigDecimal amount, PaymentStrategy paymentStrategy) {}
