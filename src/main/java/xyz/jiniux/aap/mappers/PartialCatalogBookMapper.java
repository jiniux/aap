package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.EditBookRequest;
import xyz.jiniux.aap.domain.catalog.PartialCatalogBook;

@Mapper
public interface PartialCatalogBookMapper {
    PartialCatalogBookMapper MAPPER = Mappers.getMapper(PartialCatalogBookMapper.class);

    PartialCatalogBook fromEditBookRequest(EditBookRequest editBookRequest);
}
