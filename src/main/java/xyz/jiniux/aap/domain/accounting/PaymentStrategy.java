package xyz.jiniux.aap.domain.accounting;

import xyz.jiniux.aap.domain.model.PaymentState;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentExecutionResult execute(BigDecimal amount);
}
