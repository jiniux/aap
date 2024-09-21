package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.results.FullPublisherResult;
import xyz.jiniux.aap.domain.catalog.results.PublisherSearchResult;
import xyz.jiniux.aap.model.Publisher;

import java.util.List;

@Mapper
public interface FullPublisherResultMapper {
    FullPublisherResultMapper MAPPER = Mappers.getMapper(FullPublisherResultMapper.class);

    FullPublisherResult fromPublishers(Publisher publisher);
}
