package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.BookSearchResultEntry;
import xyz.jiniux.aap.domain.model.CatalogBook;

import java.util.List;

@Mapper
public interface BookSearchResultEntryMapper {
    BookSearchResultEntryMapper MAPPER = Mappers.getMapper(BookSearchResultEntryMapper.class);
    List<BookSearchResultEntry> fromCatalogBooks(List<CatalogBook> book);
}
