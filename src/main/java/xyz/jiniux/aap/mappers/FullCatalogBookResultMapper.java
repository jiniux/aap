package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.catalog.requests.BookRegistrationRequest;
import xyz.jiniux.aap.domain.catalog.results.FullCatalogBookResult;
import xyz.jiniux.aap.model.CatalogBook;
import xyz.jiniux.aap.support.ISBNCleaner;

@Mapper
public interface FullCatalogBookResultMapper {
    FullCatalogBookResultMapper MAPPER = Mappers.getMapper(FullCatalogBookResultMapper.class);

    FullCatalogBookResult fromCatalogBook(CatalogBook catalogBook);
}
