package xyz.jiniux.aap.mappers;

import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.EditAuthorRequest;
import xyz.jiniux.aap.controllers.requests.EditPublisherRequest;
import xyz.jiniux.aap.domain.catalog.PartialAuthor;
import xyz.jiniux.aap.domain.catalog.PartialPublisher;

public interface PartialPublisherMapper {
    PartialPublisherMapper MAPPER = Mappers.getMapper(PartialPublisherMapper.class);

    PartialPublisher fromEditPublisherRequest(EditPublisherRequest editPublisherRequest);
}
