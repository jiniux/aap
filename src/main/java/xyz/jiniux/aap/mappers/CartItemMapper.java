package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.CheckoutRequestCartItem;
import xyz.jiniux.aap.controllers.requests.SyncShoppingCartRequestItem;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.support.ISBNCleaner;

import java.util.List;
import java.util.Set;

@Mapper
public interface CartItemMapper {
    CartItemMapper MAPPER = Mappers.getMapper(CartItemMapper.class);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "parseStockFormat")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "parseStockQuality")
    ShoppingCart.Item fromSyncCartRequestItem(SyncShoppingCartRequestItem item);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "parseStockFormat")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "parseStockQuality")
    ShoppingCart.Item fromCheckoutRequestItem(CheckoutRequestCartItem item);

    List<ShoppingCart.Item> fromCheckoutRequestItems(Set<CheckoutRequestCartItem> item);

    List<ShoppingCart.Item> fromSyncCartRequestItems(List<SyncShoppingCartRequestItem> items);

    @Named("parseStockQuality")
    static StockQuality parseStockQuality(String value) {
        return StockQualityMapper.MAPPER.fromString(value);
    }

    @Named("parseStockFormat")
    static StockFormat parseStockFormat(String value) {
        return StockFormatMapper.MAPPER.fromString(value);
    }

    @Named("cleanIsbn")
    static String cleanIsbn(String value) {
        return ISBNCleaner.clean(value);
    }
}
