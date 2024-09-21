package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.requests.BookRegistrationRequest;
import xyz.jiniux.aap.model.CatalogBook;
import xyz.jiniux.aap.support.ISBNCleaner;

@Mapper
public interface CatalogBookMapper {
    CatalogBookMapper MAPPER = Mappers.getMapper(CatalogBookMapper.class);

    @Mapping(target = "isbn", source = "request.isbn", qualifiedByName = "cleanIsbn")
    CatalogBook fromBookRegistrationRequest(BookRegistrationRequest request);

    @Named("cleanIsbn")
    static String cleanIsbn(String isbn) {
        return ISBNCleaner.clean(isbn);
    }
}
