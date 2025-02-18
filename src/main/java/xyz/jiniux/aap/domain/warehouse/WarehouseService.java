package xyz.jiniux.aap.domain.warehouse;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.warehouse.exceptions.*;
import xyz.jiniux.aap.infrastructure.persistency.CatalogBookRepository;
import xyz.jiniux.aap.infrastructure.persistency.StockRepository;
import xyz.jiniux.aap.domain.model.CatalogBook;
import xyz.jiniux.aap.domain.model.Stock;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseService {
    private final CatalogBookRepository catalogBookRepository;
    private final StockRepository stockRepository;

    public WarehouseService(CatalogBookRepository catalogBookRepository, StockRepository stockRepository) {
        this.catalogBookRepository = catalogBookRepository;
        this.stockRepository = stockRepository;
    }

    private Stock getOrCreateStockForUpdate(long bookId, StockFormat format, StockQuality quality) {
        return stockRepository.findByBookIdForUpdate(bookId, format, quality)
            .orElseGet(() -> Stock.createEmpty(bookId, format, quality));
    }

    public List<Stock> getAvailableStocksByIsbn(String isbn) {
        return stockRepository.findAvailableByBookIsbn(isbn);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refillStock(
        @NonNull String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality,
        long quantity
    ) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);
        stock.addQuantity(quantity);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setStockPrice(
        String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality,
        @NonNull BigDecimal priceEur
    )
        throws BookNotFoundException
    {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);
        stock.setPriceEur(priceEur);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void putStockOnSale(
        String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality
    ) throws BookNotFoundException, StockAlreadyOnSaleException, StockPriceNotSetException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);

        if (stock.getPriceEur() == null)
            throw new StockPriceNotSetException(isbn, stockFormat, stockQuality);

        if (stock.isOnSale())
            throw new StockAlreadyOnSaleException(isbn, stockFormat, stockQuality);

        stock.setOnSale(true);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeStockFromSale(
        @NonNull String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality
    ) throws BookNotFoundException, StockNotOnSaleException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);

        if (!stock.isOnSale())
            throw new StockNotOnSaleException(isbn, stockFormat, stockQuality);

        stock.setOnSale(false);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(
        @NonNull String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality,
        long quantity
    ) throws BookNotFoundException, StockNotOnSaleException, NotEnoughItemsInStockException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);

        if (!stock.isOnSale())
            throw new StockNotOnSaleException(isbn, stockFormat, stockQuality);

        if (stock.getQuantity() < quantity)
            throw new NotEnoughItemsInStockException(isbn, stockFormat, stockQuality, quantity);

        stock.removeQuantity(quantity);

        stockRepository.save(stock);
    }

    public record CheckStockAvailabilityQuery(
        String isbn,
        StockFormat stockFormat,
        StockQuality stockQuality
    ) {}

    @Transactional(readOnly = true)
    public List<Long> checkStocksAvailability(List<CheckStockAvailabilityQuery> availabilityQueries) {
        return checkStocksAvailability(availabilityQueries, false);
    }

    @Transactional(readOnly = true)
    public List<Long> checkStocksAvailability(List<CheckStockAvailabilityQuery> availabilityQueries, boolean lock) {
        List<Stock> stocks;

        if (lock)
            stocks = stockRepository.bulkGetStocksByISBNs(availabilityQueries.stream().map(CheckStockAvailabilityQuery::isbn).toList());
        else
            stocks = stockRepository.bulkGetStocksByISBNsForUpdate(availabilityQueries.stream().map(CheckStockAvailabilityQuery::isbn).toList());

        Map<String, List<Stock>> stocksByIsbn = createStockMap(stocks);

        List<Long> availabilities = new ArrayList<>();
        for (CheckStockAvailabilityQuery query: availabilityQueries) {
            List<Stock> bookStocks = stocksByIsbn.get(query.isbn());

            long availability;

            if (bookStocks == null) {
                availability = 0;
            } else {
                availability = bookStocks.stream()
                    .filter(s ->
                        s.getQuality() == query.stockQuality()
                        && s.getFormat() == query.stockFormat
                    )
                    .findFirst()
                    .map(Stock::getQuantity)
                    .orElse(0L);
            }

            availabilities.add(availability);
        }

        return availabilities;
    }

    public record GetStockPriceQuery(
        String isbn,
        StockFormat stockFormat,
        StockQuality stockQuality
    ) {}

    @Transactional(readOnly = true)
    public List<BigDecimal> getStockPricesEur(List<GetStockPriceQuery> queries) {
        List<Stock> stocks = stockRepository.bulkGetStocksByISBNs(queries.stream().map(GetStockPriceQuery::isbn).toList());
        Map<String, List<Stock>> stocksByIsbn = createStockMap(stocks);

        List<BigDecimal> pricesEur = new ArrayList<>();
        for (GetStockPriceQuery query: queries) {
            List<Stock> bookStocks = stocksByIsbn.get(query.isbn());

            BigDecimal priceEur = bookStocks.stream()
                .filter(s ->
                        s.getQuality() == query.stockQuality()
                                && s.getFormat() == query.stockFormat
                )
                .findFirst()
                .map(Stock::getPriceEur)
                .orElse(BigDecimal.ONE.negate());

            pricesEur.add(priceEur);
        }

        return pricesEur;
    }

    private static Map<String, List<Stock>> createStockMap(List<Stock> stocks) {
        HashMap<String, List<Stock>> stocksByIsbn = new HashMap<>();

        for (Stock stock: stocks) {
            stocksByIsbn.computeIfAbsent(stock.getBook().getIsbn(), (_) -> new ArrayList<>()).add(stock);
        }

        return stocksByIsbn;
    }
}
