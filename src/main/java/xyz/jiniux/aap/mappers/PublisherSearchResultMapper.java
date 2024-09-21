package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.requests.PublisherRegistrationRequest;
import xyz.jiniux.aap.domain.catalog.results.BookSearchResult;
import xyz.jiniux.aap.domain.catalog.results.PublisherSearchResult;
import xyz.jiniux.aap.model.CatalogBook;
import xyz.jiniux.aap.model.Publisher;

import java.util.List;

@Mapper
public interface PublisherSearchResultMapper {
    PublisherSearchResultMapper MAPPER = Mappers.getMapper(PublisherSearchResultMapper.class);

    List<PublisherSearchResult.Entry> fromPublishers(List<Publisher> book);
}
