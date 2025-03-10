package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.EditBookRequest;
import xyz.jiniux.aap.domain.catalog.PartialCatalogBook;
import xyz.jiniux.aap.domain.model.BookCategory;

@Mapper
public interface PartialBookMapper {
    PartialBookMapper MAPPER = Mappers.getMapper(PartialBookMapper.class);

    @Mapping(target = "categories", source = "editBookRequest.categories", qualifiedByName = "categoryFromString")
    PartialCatalogBook fromEditBookRequest(EditBookRequest editBookRequest);

    @Named("categoryFromString")
    static BookCategory categoryFromString(String value) {
        return BookCategoryMapper.MAPPER.fromString(value);
    }
}
