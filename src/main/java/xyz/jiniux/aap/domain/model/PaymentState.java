package xyz.jiniux.aap.domain.model;

public enum PaymentState {
    PENDING,
    COMPLETED,
    ERRORED_UNKNOWN,
    ERRORED_NOT_ENOUGH_FUNDS;

    public boolean isErrored() {
        return this == ERRORED_UNKNOWN || this == ERRORED_NOT_ENOUGH_FUNDS;
    }
}
