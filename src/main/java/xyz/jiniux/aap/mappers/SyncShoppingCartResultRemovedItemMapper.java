package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultRemovedItem;
import xyz.jiniux.aap.domain.cart.RemovedShoppingCartItem;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;

@Mapper
public interface SyncShoppingCartResultRemovedItemMapper {
    SyncShoppingCartResultRemovedItemMapper MAPPER = Mappers.getMapper(SyncShoppingCartResultRemovedItemMapper.class);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "stockFormatToString")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "stockQualityToString")
    SyncShoppingCartResultRemovedItem fromRemovedCartItem(RemovedShoppingCartItem item);

    List<SyncShoppingCartResultRemovedItem> fromRemovedCartItems(List<RemovedShoppingCartItem> items);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }
}
