package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.CheckoutRequestAddress;
import xyz.jiniux.aap.domain.model.Address;

@Mapper
public interface AddressMapper {
    AddressMapper MAPPER = Mappers.getMapper(AddressMapper.class);

    Address fromCheckoutRequestAddress(CheckoutRequestAddress address);
}
