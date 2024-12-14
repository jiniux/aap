package xyz.jiniux.aap.domain.order.exceptions;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends Exception {
    public OrderNotFoundException(Long orderId) {
        super("Order with id " + orderId + " not found");
        this.orderId = orderId;
    }

    private final Long orderId;
}
