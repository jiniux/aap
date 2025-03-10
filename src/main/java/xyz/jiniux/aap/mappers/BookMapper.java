package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.BookRegistrationRequest;
import xyz.jiniux.aap.domain.model.Book;
import xyz.jiniux.aap.domain.model.BookCategory;
import xyz.jiniux.aap.support.ISBNCleaner;

@Mapper
public interface BookMapper {
    BookMapper MAPPER = Mappers.getMapper(BookMapper.class);

    @Mapping(target = "isbn", source = "request.isbn", qualifiedByName = "cleanIsbn")
    @Mapping(target = "categories", source = "request.categories", qualifiedByName = "categoryFromString")
    Book fromBookRegistrationRequest(BookRegistrationRequest request);

    @Named("categoryFromString")
    static BookCategory categoryFromString(String value) {
        return BookCategoryMapper.MAPPER.fromString(value);
    }

    @Named("cleanIsbn")
    static String cleanIsbn(String isbn) {
        return ISBNCleaner.clean(isbn);
    }
}
