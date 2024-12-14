package xyz.jiniux.aap.domain.accounting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.jiniux.aap.domain.model.PaymentState;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

@AllArgsConstructor
@Getter
public class CreditCardPaymentStrategy implements PaymentStrategy {
    private CreditCardDetails creditCardDetails;

    @Override
    public PaymentExecutionResult execute(BigDecimal amount) {
        RandomGenerator randomGenerator = RandomGenerator.getDefault();

        try {
            TimeUnit.SECONDS.sleep(randomGenerator.nextInt(60, 70));
        } catch (InterruptedException e) {
            // ignored
        }

        double realization = randomGenerator.nextDouble();

        if (realization <= 0.1) {
            return PaymentExecutionResult.ERRORED_UNKNOWN;
        }

        return PaymentExecutionResult.COMPLETED;
    }
}
