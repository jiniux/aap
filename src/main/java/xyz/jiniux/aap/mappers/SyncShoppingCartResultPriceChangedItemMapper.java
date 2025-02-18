package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultPriceChangedItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultRemovedItem;
import xyz.jiniux.aap.domain.cart.PriceChangedShoppingCartItem;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;

@Mapper
public interface SyncShoppingCartResultPriceChangedItemMapper {
    SyncShoppingCartResultPriceChangedItemMapper MAPPER = Mappers.getMapper(SyncShoppingCartResultPriceChangedItemMapper.class);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "stockFormatToString")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "stockQualityToString")
    SyncShoppingCartResultRemovedItem fromPriceChangedCartItems(PriceChangedShoppingCartItem item);

    List<SyncShoppingCartResultPriceChangedItem> fromPriceChangedCartItems(List<PriceChangedShoppingCartItem> items);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }
}
