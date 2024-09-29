package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.PublisherSearchResultEntry;
import xyz.jiniux.aap.domain.model.Publisher;

import java.util.List;

@Mapper
public interface PublisherSearchResultEntryMapper {
    PublisherSearchResultEntryMapper MAPPER = Mappers.getMapper(PublisherSearchResultEntryMapper.class);

    List<PublisherSearchResultEntry> fromPublishers(List<Publisher> book);
}
