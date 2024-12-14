package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.jiniux.aap.domain.model.Stock;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.book.isbn = :isbn and s.quality = :quality and s.format = :format")
    Optional<Stock> findByIsbnForUpdate(@Param("isbn") String isbn, @Param("format") StockFormat format, @Param("quality") StockQuality quality);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.book.id = :bookId and s.quality = :quality and s.format = :format")
    Optional<Stock> findByBookIdForUpdate(@Param("bookId") long bookId, @Param("format") StockFormat format, @Param("quality") StockQuality quality);

    @Query("select s from Stock s join s.book where s.book.isbn in :isbns ")
    List<Stock> bulkGetStocksByISBNs(@Param("isbns") List<String> isbns);

    @Query("select s from Stock s join s.book where s.book.isbn = :isbn and s.quantity > 0")
    List<Stock> findAvailableByBookIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from Stock s join s.book where s.book.isbn in :isbns ")
    List<Stock> bulkGetStocksByISBNsForUpdate(@Param("isbns") List<String> isbns);
}
