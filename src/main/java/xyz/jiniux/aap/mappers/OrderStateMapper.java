package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.CheckoutRequestAddress;
import xyz.jiniux.aap.controllers.requests.ShipmentCostRequest;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.model.OrderState;
import xyz.jiniux.aap.domain.model.StockQuality;

@Mapper
public interface OrderStateMapper {
    OrderStateMapper MAPPER = Mappers.getMapper(OrderStateMapper.class);

    @ValueMapping(target = "PROCESSING_PAYMENT", source = "processing-payment")
    @ValueMapping(target = "PAYMENT_FAILED", source = "payment-failed")
    @ValueMapping(target = "CONFIRMED", source = "confirmed")
    @ValueMapping(target = "SHIPPED", source = "shipped")
    @ValueMapping(target = "DELIVERED", source = "delivered")
    @ValueMapping(target = "WAITING_CONFIRMATION", source = "waiting-confirmation")
    OrderState fromString(String value);

    @ValueMapping(target = "processing-payment", source = "PROCESSING_PAYMENT")
    @ValueMapping(target = "payment-failed", source = "PAYMENT_FAILED")
    @ValueMapping(target = "confirmed", source = "CONFIRMED")
    @ValueMapping(target = "shipped", source = "SHIPPED")
    @ValueMapping(target = "delivered", source = "DELIVERED")
    @ValueMapping(target = "waiting-confirmation", source = "WAITING_CONFIRMATION")
    String toString(OrderState value);
}
