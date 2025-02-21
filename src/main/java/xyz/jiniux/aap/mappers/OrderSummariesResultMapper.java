package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.OrderSummariesResult;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.OrderState;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper
public interface OrderSummariesResultMapper {
    OrderSummariesResultMapper MAPPER = Mappers.getMapper(OrderSummariesResultMapper.class);

    @Mapping(target = "state", source = "order.state", qualifiedByName = "orderStateToString")
    @Mapping(target = "placeAt", source = "order.placedAt", qualifiedByName = "orderDateIso8601")
    @Mapping(target = "totalEur", source = "order.finalPrice")
    @Mapping(target = "id", source = "order.id")
    OrderSummariesResult.OrderSummary fromOrder(Order order);

    List<OrderSummariesResult.OrderSummary> fromOrders(List<Order> orders);

    @Named("orderStateToString")
    static String orderStateToString(OrderState value) {
        return OrderStateMapper.MAPPER.toString(value);
    }

    @Named("orderDateIso8601")
    static String orderDateIso8601(OffsetDateTime value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX");
        return value.format(formatter);
    }
}
