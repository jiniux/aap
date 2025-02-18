package xyz.jiniux.aap.controllers;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ErrorResponse(String code, Object details) implements Serializable {
    public static ErrorResponse createAuthorsNotFound(Set<String> authorIds) {
        return new ErrorResponse("AUTHORS_DO_NOT_EXIST", Map.of("authorIds", authorIds));
    }

    public static ErrorResponse createIsbnAlreadyRegistered(String isbn) {
        return new ErrorResponse("ISBN_ALREADY_REGISTERED", Map.of("isbn", isbn));
    }

    public static ErrorResponse createPublisherDoesNotExist(String publisherId) {
        return new ErrorResponse("PUBLISHER_DOES_NOT_EXIST", Map.of("publisherId", publisherId));
    }

    public static ErrorResponse createBookNotFound(String isbn) {
        return new ErrorResponse("BOOK_NOT_FOUND", Map.of("isbn", isbn));
    }

    public static ErrorResponse createDataIntegrityViolation() {
        return new ErrorResponse("DATA_INTEGRITY_VIOLATION", Map.of());
    }

    public static ErrorResponse createConcurrentModificationOccurred() {
        return new ErrorResponse("CONCURRENT_MODIFICATION_OCCURRED", Map.of());
    }

    public static ErrorResponse createPublisherNotFound(String publisherId) {
        return new ErrorResponse("PUBLISHER_NOT_FOUND", Map.of("publisherId", publisherId));
    }

    public static ErrorResponse createPublisherHasBooks(String publisherId) {
        return new ErrorResponse("PUBLISHER_HAS_BOOKS", Map.of("publisherId", publisherId));
    }

    public static ErrorResponse createAuthorNotFound(String authorId) {
        return new ErrorResponse("AUTHOR_NOT_FOUND", Map.of("authorId", authorId));
    }

    public static ErrorResponse createAuthorHasBooks(String authorId) {
        return new ErrorResponse("AUTHOR_HAS_BOOKS", Map.of("authorId", authorId));
    }

    public static ErrorResponse createNoAuthorSpecified() {
        return new ErrorResponse("NO_AUTHOR_SPECIFIED", Map.of());
    }

    public static ErrorResponse createEbookOnlySupportsDigitalQuality() {
        return new ErrorResponse("EBOOK_ONLY_SUPPORTS_DIGITAL_QUALITY", Map.of());
    }


    public static ErrorResponse createStocksNotAvailable(List<ImmutableTriple<String, StockFormat, StockQuality>> stocks) {
        return new ErrorResponse("STOCKS_QUANTITY_NOT_AVAILABLE", Map.of("stocks", stocks));
    }

    public static ErrorResponse createStockAlreadyOnSale(String isbn, String stockQuality, String stockFormat) {
        return new ErrorResponse("STOCK_ALREADY_ON_SALE", Map.of("isbn", isbn, "stockQuality", stockQuality, "stockFormat", stockFormat));
    }

    public static ErrorResponse createStockNotOnSale(String isbn, String stockQuality, String stockFormat) {
        return new ErrorResponse("STOCK_NOT_ON_SALE", Map.of("isbn", isbn, "stockQuality", stockQuality, "stockFormat", stockFormat));
    }

    public static ErrorResponse createNotEnoughItemsInStock(String isbn, String stockQuality, String stockFormat) {
        return new ErrorResponse("NOT_ENOUGH_ITEMS_IN_STOCK", Map.of("isbn", isbn, "stockQuality", stockQuality, "stockFormat", stockFormat));
    }

    public static ErrorResponse createStockQuantityNotAvailable(String isbn, String stockQuality, String stockFormat) {
        return new ErrorResponse("STOCK_QUANTITY_NOT_AVAILABLE", Map.of("isbn", isbn, "stockQuality", stockQuality, "stockFormat", stockFormat));
    }

    public static ErrorResponse createItemsPriceChanged(List<ItemsPriceChangedWhilePlacingOrderException.Info> details) {
        var info = details.stream().map(i -> Map.of("isbn", i.bookIsbn(), "stockFormat", i.stockFormat(), "stockQuality", i.stockQuality(), "oldPrice", i.oldPrice(), "newPrice", i.newPrice())).toList();
        return new ErrorResponse("ITEMS_PRICE_CHANGED", info);
    }

    public static Object createImageTooBig(long maxSizeInBytes, long actualSizeInBytes) {
        return new ErrorResponse("IMAGE_TOO_BIG", Map.of("maxSizeInBytes", maxSizeInBytes, "actualSizeInBytes", actualSizeInBytes));
    }

    public static Object createInvalidImageFormat(Set<String> expectedFormats, String actualFormat) {
        return new ErrorResponse("INVALID_IMAGE_FORMAT", Map.of("expectedFormats", expectedFormats, "actualFormat", actualFormat));
    }

    public static Object createStockPriceNotSet(String isbn, String stockFormat, String stockQuality) {
        return new ErrorResponse("STOCK_PRICE_NOT_SET", Map.of("isbn", isbn, "stockFormat", stockFormat, "stockQuality", stockQuality));
    }
}
