package xyz.jiniux.aap.domain.warehouse;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockAlreadyNotOnSaleException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockAlreadyOnSaleException;
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

    @Transactional
    public void refillStock(
        @NonNull String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality,
        int quantity
    ) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);
        stock.addQuantity(quantity);

        stockRepository.save(stock);
    }

    @Transactional
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

    @Transactional
    public void putStockOnSale(
        String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality
    ) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);

        if (stock.isOnSale())
            throw new StockAlreadyOnSaleException(isbn, stockFormat, stockQuality);

        stock.setOnSale(true);

        stockRepository.save(stock);
    }

    @Transactional
    public void removeStockFromSale(
        @NonNull String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality
    ) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(catalogBook.getId(), stockFormat, stockQuality);

        if (!stock.isOnSale())
            throw new StockAlreadyNotOnSaleException(isbn, stockFormat, stockQuality);

        stock.setOnSale(false);

        stockRepository.save(stock);
    }

    public record CheckStockAvailabilityQuery(
        String isbn,
        StockFormat stockFormat,
        StockQuality stockQuality
    ) {}

    public List<Integer> checkStocksAvailability(List<CheckStockAvailabilityQuery> availabilityQueries) {
        return checkStocksAvailability(availabilityQueries, false);
    }

    @Transactional(readOnly = true)
    public List<Integer> checkStocksAvailability(List<CheckStockAvailabilityQuery> availabilityQueries, boolean lock) {
        HashMap<String, List<Stock>> stocksByISBN = new HashMap<>();

        List<Stock> stocks;

        if (lock)
            stocks = stockRepository.bulkGetStocksByISBNs(availabilityQueries.stream().map(CheckStockAvailabilityQuery::isbn).toList());
        else
            stocks = stockRepository.bulkGetStocksByISBNsForUpdate(availabilityQueries.stream().map(CheckStockAvailabilityQuery::isbn).toList());

        for (Stock stock: stocks) {
            stocksByISBN.computeIfAbsent(stock.getBook().getIsbn(), (k) -> new ArrayList<>()).add(stock);
        }

        List<Integer> availabilities = new ArrayList<>();
        for (CheckStockAvailabilityQuery query: availabilityQueries) {
            List<Stock> bookStocks = stocksByISBN.get(query.isbn());

            int availability;

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
                    .orElse(0);
            }

            availabilities.add(availability);
        }

        return availabilities;
    }
}
