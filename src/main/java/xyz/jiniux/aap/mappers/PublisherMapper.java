package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.PublisherRegistrationRequest;
import xyz.jiniux.aap.domain.model.Publisher;

@Mapper
public interface PublisherMapper {
    PublisherMapper MAPPER = Mappers.getMapper(PublisherMapper.class);

    Publisher fromPublisherRegistrationRequest(PublisherRegistrationRequest request);
}
