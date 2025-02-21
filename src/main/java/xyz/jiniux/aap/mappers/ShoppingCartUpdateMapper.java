package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.AddItemShoppingCartRequestItem;
import xyz.jiniux.aap.controllers.requests.RemoveItemShoppingCartRequestItem;
import xyz.jiniux.aap.controllers.requests.UpdateItemShoppingCartRequestItem;
import xyz.jiniux.aap.domain.cart.ShoppingCartUpdate;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

@Mapper
public interface ShoppingCartUpdateMapper {
    ShoppingCartUpdateMapper MAPPER = Mappers.getMapper(ShoppingCartUpdateMapper.class);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "parseStockFormat")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "parseStockQuality")
    ShoppingCartUpdate.AddItem fromAddItemRequestItem(AddItemShoppingCartRequestItem item);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "parseStockFormat")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "parseStockQuality")
    ShoppingCartUpdate.RemoveItem fromRemoveItemRequestItem(RemoveItemShoppingCartRequestItem item);

    @Mapping(target = "stockFormat", source = "item.stockFormat", qualifiedByName = "parseStockFormat")
    @Mapping(target = "stockQuality", source = "item.stockQuality", qualifiedByName = "parseStockQuality")
    ShoppingCartUpdate.UpdateItem fromUpdateItemRequestItem(UpdateItemShoppingCartRequestItem item);

    @Named("parseStockQuality")
    static StockQuality parseStockQuality(String value) {
        return StockQualityMapper.MAPPER.fromString(value);
    }

    @Named("parseStockFormat")
    static StockFormat parseStockFormat(String value) {
        return StockFormatMapper.MAPPER.fromString(value);
    }
}
