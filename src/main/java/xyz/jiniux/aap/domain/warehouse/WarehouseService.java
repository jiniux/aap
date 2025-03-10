package xyz.jiniux.aap.domain.warehouse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Tuple;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.warehouse.exceptions.*;
import xyz.jiniux.aap.infrastructure.persistency.BookRepository;
import xyz.jiniux.aap.infrastructure.persistency.StockRepository;
import xyz.jiniux.aap.domain.model.Book;
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
    private final BookRepository bookRepository;
    private final StockRepository stockRepository;
    private final EntityManager entityManager;

    public WarehouseService(
        BookRepository bookRepository,
        StockRepository stockRepository,
        EntityManager entityManager
    ) {
        this.bookRepository = bookRepository;
        this.stockRepository = stockRepository;
        this.entityManager = entityManager;
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
        Book book = bookRepository.findBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(book.getId(), stockFormat, stockQuality);
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
        Book book = bookRepository.findBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(book.getId(), stockFormat, stockQuality);
        stock.setPriceEur(priceEur);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void putStockOnSale(
        String isbn,
        @NonNull StockFormat stockFormat,
        @NonNull StockQuality stockQuality
    ) throws BookNotFoundException, StockAlreadyOnSaleException, StockPriceNotSetException {
        Book book = bookRepository.findBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(book.getId(), stockFormat, stockQuality);

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
        Book book = bookRepository.findBookByIsbnForShare(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        Stock stock = getOrCreateStockForUpdate(book.getId(), stockFormat, stockQuality);

        if (!stock.isOnSale())
            throw new StockNotOnSaleException(isbn, stockFormat, stockQuality);

        stock.setOnSale(false);

        stockRepository.save(stock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(Stock stock, long quantity, boolean lock) throws NotEnoughItemsInStockException, StockNotOnSaleException {
        try {
            if (lock) {
                entityManager.refresh(stock, LockModeType.PESSIMISTIC_WRITE);
            }
        } catch (EntityNotFoundException e) {
            throw new StockNotOnSaleException(stock.getBook().getIsbn(), stock.getFormat(), stock.getQuality());
        }

        if (!stock.isOnSale())
            throw new StockNotOnSaleException(stock.getBook().getIsbn(), stock.getFormat(), stock.getQuality());

        if (stock.getQuantity() < quantity)
            throw new NotEnoughItemsInStockException(stock.getBook().getIsbn(), stock.getFormat(), stock.getQuality(), quantity);

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
        List<Tuple> stocks;

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

    public Map<String, List<Stock>> bulkGetStocksByISBNsForUpdate(List<String> isbns) {
        List<Tuple> stocks = stockRepository.bulkGetStocksByISBNsForUpdate(isbns);
        return createStockMap(stocks);
    }

    @Transactional(readOnly = true)
    public List<BigDecimal> getStockPricesEur(List<GetStockPriceQuery> queries) {
        List<Tuple> stocks = stockRepository.bulkGetStocksByISBNs(queries.stream().map(GetStockPriceQuery::isbn).toList());
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

    private static Map<String, List<Stock>> createStockMap(List<Tuple> tuples) {
        HashMap<String, List<Stock>> stocksByIsbn = new HashMap<>();

        for (Tuple tuple: tuples) {

            Stock stock = tuple.get("stock", Stock.class);
            String isbn = tuple.get("isbn", String.class);

            stocksByIsbn.computeIfAbsent(isbn, (_) -> new ArrayList<>()).add(stock);
        }

        return stocksByIsbn;
    }
}
