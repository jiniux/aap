package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.results.BookSearchResultEntry;
import xyz.jiniux.aap.domain.model.*;

import java.util.List;

@Mapper
public interface BookSearchResultEntryMapper {
    BookSearchResultEntryMapper MAPPER = Mappers.getMapper(BookSearchResultEntryMapper.class);

    @Mapping(target = "format", source = "format", qualifiedByName = "stockFormatToString")
    @Mapping(target = "url", source = "id", qualifiedByName = "stockPreviewImageIdToUrl")
    BookSearchResultEntry.EntryFormatPreviewImages fromStockPreviewImage(BookFormatPreviewImage images);

    @Mapping(target = "format", source = "stock.format", qualifiedByName = "stockFormatToString")
    @Mapping(target = "quality", source = "stock.quality", qualifiedByName = "stockQualityToString")
    BookSearchResultEntry.EntryStock fromStock(Stock stock);

    @Named("stockQualityToString")
    static String stockQualityToString(StockQuality value) {
        return StockQualityMapper.MAPPER.toString(value);
    }

    @Named("stockFormatToString")
    static String stockFormatToString(StockFormat value) {
        return StockFormatMapper.MAPPER.toString(value);
    }

    @Named("stockPreviewImageIdToUrl")
    static String stockPreviewImageIdToUrl(Long id) {
        return "/book-format-preview-images/" + id;
    }

    @Mapping(target = "stocks", source = "stocks")
    List<BookSearchResultEntry> fromCatalogBooks(List<Book> book);

}
