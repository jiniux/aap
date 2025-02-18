package xyz.jiniux.aap.domain.billing.events;

import xyz.jiniux.aap.domain.model.Payment;
import xyz.jiniux.aap.domain.model.PaymentState;

public record PaymentStateChangeEvent(long paymentId, PaymentState state) {
    public static PaymentStateChangeEvent fromPayment(Payment payment) {
        return new PaymentStateChangeEvent(payment.getId(), payment.getState());
    }
}
