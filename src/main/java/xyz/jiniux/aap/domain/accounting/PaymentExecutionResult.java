package xyz.jiniux.aap.domain.accounting;

import xyz.jiniux.aap.domain.model.PaymentState;

public enum PaymentExecutionResult {
    COMPLETED,
    ERRORED_NOT_ENOUGH_FUNDS,
    ERRORED_UNKNOWN;

    public PaymentState toPaymentState() {
        return switch (this) {
            case COMPLETED -> PaymentState.COMPLETED;
            case ERRORED_NOT_ENOUGH_FUNDS -> PaymentState.ERRORED_NOT_ENOUGH_FOUNDS;
            default -> PaymentState.ERRORED_UNKNOWN;
        };
    }
}
