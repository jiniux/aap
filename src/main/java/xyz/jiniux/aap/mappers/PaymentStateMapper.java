package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.model.OrderState;
import xyz.jiniux.aap.domain.model.PaymentState;

@Mapper
public interface PaymentStateMapper {
    PaymentStateMapper MAPPER = Mappers.getMapper(PaymentStateMapper.class);

    @ValueMapping(target = "PENDING", source = "pending")
    @ValueMapping(target = "COMPLETED", source = "completed")
    @ValueMapping(target = "ERRORED_UNKNOWN", source = "errored-unknown")
    @ValueMapping(target = "ERRORED_NOT_ENOUGH_FUNDS", source = "errored-not-enough-funds")
    PaymentState fromString(String value);

    @ValueMapping(target = "pending", source = "PENDING")
    @ValueMapping(target = "completed", source = "COMPLETED")
    @ValueMapping(target = "errored-unknown", source = "ERRORED_UNKNOWN")
    @ValueMapping(target = "errored-not-enough-funds", source = "ERRORED_NOT_ENOUGH_FUNDS")
    String toString(PaymentState value);
}
