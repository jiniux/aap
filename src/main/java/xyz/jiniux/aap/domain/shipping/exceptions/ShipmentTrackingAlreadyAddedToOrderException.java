package xyz.jiniux.aap.domain.shipping.exceptions;

import lombok.Getter;

@Getter
public class ShipmentTrackingAlreadyAddedToOrderException extends Exception {
    public ShipmentTrackingAlreadyAddedToOrderException(Long orderId) {
        super("Shipment tracking already added to order with id " + orderId);
        this.orderId = orderId;
    }

    private final Long orderId;
}
