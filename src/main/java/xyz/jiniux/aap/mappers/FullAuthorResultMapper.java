package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.results.FullAuthorResult;
import xyz.jiniux.aap.domain.catalog.results.FullPublisherResult;
import xyz.jiniux.aap.model.Author;
import xyz.jiniux.aap.model.Publisher;

@Mapper
public interface FullAuthorResultMapper {
    FullAuthorResultMapper MAPPER = Mappers.getMapper(FullAuthorResultMapper.class);

    FullAuthorResult fromAuthor(Author author);
}
