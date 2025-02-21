package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.FullOrderResult;
import xyz.jiniux.aap.domain.model.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface FullOrderResultMapper {
    FullOrderResultMapper MAPPER = Mappers.getMapper(FullOrderResultMapper.class);

    FullOrderResult.OrderAddress fromAddress(Address address);

    @Mapping(target = "stockFormat", source = "orderItem.stockFormat", qualifiedByName = "stockFormatToString")
    @Mapping(target = "stockQuality", source = "orderItem.stockQuality", qualifiedByName = "stockQualityToString")
    FullOrderResult.OrderItem fromOrderItem(Order.Item orderItem);

    @Mapping(target = "state", source = "payment.state", qualifiedByName = "paymentStateToString")
    @Mapping(target = "method", source = "payment.method", qualifiedByName = "paymentMethodToString")
    FullOrderResult.OrderPayment fromPayment(Payment payment);

    @Mapping(target = "state", source = "order.state", qualifiedByName = "orderStateToString")
    @Mapping(target = "placeAt", source = "order.placedAt", qualifiedByName = "orderDateIso8601")
    @Mapping(target = "totalEur", source = "order.finalPrice")
    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "shipmentCostEur", source = "order.shipmentCost")
    FullOrderResult fromOrder(Order order);

    @Named("orderStateToString")
    static String orderStateToString(OrderState value) {
        return OrderStateMapper.MAPPER.toString(value);
    }

    @Named("orderDateIso8601")
    static String orderDateIso8601(OffsetDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
        return value.format(formatter);
    }

    @Named("paymentStateToString")
    static String paymentStateToString(PaymentState value) {
        return PaymentStateMapper.MAPPER.toString(value);
    }

    @Named("paymentMethodToString")
    static String paymentMethodToString(PaymentMethod value) {
        return PaymentMethodMapper.MAPPER.toString(value);
    }

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }
}
