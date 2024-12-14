package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.StockResultElement;
import xyz.jiniux.aap.domain.model.Stock;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;

@Mapper
public interface StockResultElementMapper {
    StockResultElementMapper MAPPER = Mappers.getMapper(StockResultElementMapper.class);

    @Mapping(target = "format", source = "item.format", qualifiedByName = "stockFormatToString")
    @Mapping(target = "quality", source = "item.quality", qualifiedByName = "stockQualityToString")
    @Mapping(target = "isbn", source = "item.book.isbn")
    StockResultElement fromStock(Stock item);

    List<StockResultElement> fromStocks(List<Stock> items);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }
}
