package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.FullCatalogBookResult;
import xyz.jiniux.aap.domain.model.CatalogBook;

@Mapper
public interface FullCatalogBookResultMapper {
    FullCatalogBookResultMapper MAPPER = Mappers.getMapper(FullCatalogBookResultMapper.class);

    FullCatalogBookResult fromCatalogBook(CatalogBook catalogBook);
}
