package xyz.jiniux.aap.controllers.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import xyz.jiniux.aap.domain.accounting.CreditCardDetails;
import xyz.jiniux.aap.domain.accounting.CreditCardPaymentStrategy;
import xyz.jiniux.aap.domain.accounting.PaymentStrategy;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CheckoutRequestPaymentStrategy.CreditCard.class, name = "credit_card") })
public sealed interface CheckoutRequestPaymentStrategy {
    PaymentStrategy convert();

    record CreditCard(
        @Pattern(regexp = "[0-9]{16}")
        String number,

        @Min(1)
        @Max(12)
        int validMonth,

        @Min(2000)
        int validYear,

        @Pattern(regexp = "[a-zA-Z0-9. ]+")
        String address,

        @Pattern(regexp = "[a-zA-Z0-9 ]+")
        String tenant,

        @Min(100)
        @Max(999)
        int csc
    ) implements CheckoutRequestPaymentStrategy {
        @Override
        public PaymentStrategy convert() {
            return new CreditCardPaymentStrategy(new CreditCardDetails(number, validMonth, validYear, address, tenant, csc));
        }
    }
}
