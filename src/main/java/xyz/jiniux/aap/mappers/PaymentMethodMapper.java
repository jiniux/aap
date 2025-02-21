package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.model.PaymentMethod;
import xyz.jiniux.aap.domain.model.PaymentState;

@Mapper
public interface PaymentMethodMapper {
    PaymentMethodMapper MAPPER = Mappers.getMapper(PaymentMethodMapper.class);

    @ValueMapping(source = "credit-card", target = "CREDIT_CARD")
    PaymentMethod fromString(String value);

    @ValueMapping(source = "CREDIT_CARD", target = "credit-card")
    String toString(PaymentMethod value);
}
