package xyz.jiniux.aap.domain.order.exceptions;

import lombok.Getter;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class ShipmentCostChangedException extends Exception {
    public ShipmentCostChangedException(BigDecimal shipmentCost) {
        super("the shipment cost has changed to " + shipmentCost);
        this.shipmentCost = shipmentCost;
    }

    private final BigDecimal shipmentCost;
}
