package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultItem;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;

@Mapper
public interface SyncShoppingCartResultItemMapper {
    SyncShoppingCartResultItemMapper MAPPER = Mappers.getMapper(SyncShoppingCartResultItemMapper.class);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "stockFormatToString")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "stockQualityToString")
    SyncShoppingCartResultItem fromCartItem(ShoppingCart.Item item);

    List<SyncShoppingCartResultItem> fromCartItems(List<ShoppingCart.Item> items);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }
}
