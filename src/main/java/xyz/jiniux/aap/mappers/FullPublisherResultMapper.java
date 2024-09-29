package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.FullPublisherResult;
import xyz.jiniux.aap.domain.model.Publisher;

@Mapper
public interface FullPublisherResultMapper {
    FullPublisherResultMapper MAPPER = Mappers.getMapper(FullPublisherResultMapper.class);

    FullPublisherResult fromPublishers(Publisher publisher);
}
