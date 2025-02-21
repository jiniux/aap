package xyz.jiniux.aap.domain.billing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.jiniux.aap.domain.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.Map;
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

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public Object getAdditionalInfo() {
        return Map.of(
                "number", creditCardDetails.getHiddenNumber(),
                "tenant", creditCardDetails.tenant(),
                "expiration", creditCardDetails.validMonth() + "/" + creditCardDetails.validYear());
    }
}
