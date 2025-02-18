package xyz.jiniux.aap.domain.billing;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentExecutionResult execute(BigDecimal amount);
}
