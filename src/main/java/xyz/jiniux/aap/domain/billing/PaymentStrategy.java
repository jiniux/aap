package xyz.jiniux.aap.domain.billing;

import xyz.jiniux.aap.domain.model.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentExecutionResult execute(BigDecimal amount);

    PaymentMethod getMethod();

    Object getAdditionalInfo();
}
