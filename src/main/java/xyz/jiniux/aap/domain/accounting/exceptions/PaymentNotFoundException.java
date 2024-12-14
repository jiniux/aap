package xyz.jiniux.aap.domain.accounting.exceptions;

public class PaymentNotFoundException extends Exception {
    public PaymentNotFoundException(long paymentId) {
        super("Payment with id " + paymentId + " was not found");
    }
}
