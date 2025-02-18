package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.model.StockFormat;

@Mapper
public interface StockFormatMapper {
    StockFormatMapper MAPPER = Mappers.getMapper(StockFormatMapper.class);

    @ValueMapping(target = "HARDCOVER", source = "hardcover")
    @ValueMapping(target = "PAPERBACK", source = "paperback")
    StockFormat fromString(String value);

    @ValueMapping(target = "hardcover", source = "HARDCOVER")
    @ValueMapping(target = "paperback", source = "PAPERBACK")
    String toString(StockFormat value);
}
