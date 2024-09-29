package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.model.StockQuality;

@Mapper
public interface StockQualityMapper {
    StockQualityMapper MAPPER = Mappers.getMapper(StockQualityMapper.class);

    @ValueMapping(target = "NEW", source = "new")
    @ValueMapping(target = "LIKE_NEW", source = "like-new")
    @ValueMapping(target = "VERY_GOOD", source = "very-good")
    @ValueMapping(target = "ACCEPTABLE", source = "acceptable")
    @ValueMapping(target = "WORN", source = "worn")
    @ValueMapping(target = "DIGITAL", source = "digital")
    StockQuality fromString(String value);

    @ValueMapping(target = "new", source = "NEW")
    @ValueMapping(target = "like-new", source = "LIKE_NEW")
    @ValueMapping(target = "very-good", source = "VERY_GOOD")
    @ValueMapping(target = "acceptable", source = "ACCEPTABLE")
    @ValueMapping(target = "worn", source = "WORN")
    @ValueMapping(target = "digital", source = "DIGITAL")
    String toString(StockQuality value);
}
