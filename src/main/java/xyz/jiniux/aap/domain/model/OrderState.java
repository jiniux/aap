package xyz.jiniux.aap.domain.model;

public enum OrderState {
    PROCESSING_PAYMENT,
    PAYMENT_FAILED,
    CONFIRMED,
    SHIPPED,
    DELIVERED
}
