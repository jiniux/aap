package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.AuthorSearchResultEntry;
import xyz.jiniux.aap.domain.model.Author;

import java.util.List;

@Mapper
public interface AuthorSearchResultEntryMapper {
    AuthorSearchResultEntryMapper MAPPER = Mappers.getMapper(AuthorSearchResultEntryMapper.class);

    List<AuthorSearchResultEntry> fromAuthors(List<Author> authors);
}
