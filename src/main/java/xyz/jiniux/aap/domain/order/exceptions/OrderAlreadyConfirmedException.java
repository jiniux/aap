package xyz.jiniux.aap.domain.order.exceptions;

import lombok.Getter;

@Getter
public class OrderAlreadyConfirmedException extends Exception {
    public OrderAlreadyConfirmedException(Long orderId) {
        super("Order with id " + orderId + " was already confirmed");
        this.orderId = orderId;
    }

    private final Long orderId;
}
