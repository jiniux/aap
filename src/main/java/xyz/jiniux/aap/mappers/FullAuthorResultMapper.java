package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.FullAuthorResult;
import xyz.jiniux.aap.domain.model.Author;

@Mapper
public interface FullAuthorResultMapper {
    FullAuthorResultMapper MAPPER = Mappers.getMapper(FullAuthorResultMapper.class);

    FullAuthorResult fromAuthor(Author author);
}
