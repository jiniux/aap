package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.results.BookSearchResult;
import xyz.jiniux.aap.model.CatalogBook;

import java.util.List;

@Mapper
public interface BookSearchResultEntryMapper {
    BookSearchResultEntryMapper MAPPER = Mappers.getMapper(BookSearchResultEntryMapper.class);
    List<BookSearchResult.Entry> fromCatalogBooks(List<CatalogBook> book);
}
