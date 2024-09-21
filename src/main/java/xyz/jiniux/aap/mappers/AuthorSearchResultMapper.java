package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.results.AuthorSearchResult;
import xyz.jiniux.aap.domain.catalog.results.PublisherSearchResult;
import xyz.jiniux.aap.model.Author;
import xyz.jiniux.aap.model.Publisher;

import java.util.List;

@Mapper
public interface AuthorSearchResultMapper {
    AuthorSearchResultMapper MAPPER = Mappers.getMapper(AuthorSearchResultMapper.class);

    List<AuthorSearchResult.Entry> fromAuthors(List<Author> authors);
}
