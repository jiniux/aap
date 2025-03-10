package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import xyz.jiniux.aap.controllers.results.FullCatalogBookResult;
import xyz.jiniux.aap.domain.model.*;
import xyz.jiniux.aap.domain.model.Book;

@Mapper
public interface FullCatalogBookResultMapper {
    @Mapping(target = "format", source = "format", qualifiedByName = "stockFormatToString")
    @Mapping(target = "url", source = "id", qualifiedByName = "stockPreviewImageIdToUrl")
    FullCatalogBookResult.FormatPreviewImages fromStockPreviewImage(BookFormatPreviewImage images);

    @Mapping(target = "format", source = "stock.format", qualifiedByName = "stockFormatToString")
    @Mapping(target = "quality", source = "stock.quality", qualifiedByName = "stockQualityToString")
    FullCatalogBookResult.Stock fromStock(Stock stock);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }

    @Named("categoryToString")
    static String categoryToString(BookCategory value) {return BookCategoryMapper.MAPPER.toString(value);}

    @Named("stockPreviewImageIdToUrl")
    static String stockPreviewImageIdToUrl(Long id) {
        return "/book-format-preview-images/" + id;
    }

    FullCatalogBookResultMapper MAPPER = Mappers.getMapper(FullCatalogBookResultMapper.class);

    @Mapping(target = "stocks", source = "stocks")
    @Mapping(target = "categories", source = "categories", qualifiedByName = "categoryToString")
    FullCatalogBookResult fromCatalogBook(Book book);
}
