package xyz.jiniux.aap.infrastructure.persistency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.BookFormatPreviewImage;

import java.util.Optional;

@Repository
public interface BookFormatPreviewImageRepository extends JpaRepository<BookFormatPreviewImage, Long> {
    @Query("select i from BookFormatPreviewImage i, CatalogBook book where book.isbn = :isbn and i.format = :format and i.bookId = book.id")
    Optional<BookFormatPreviewImage> findByQualityAndFormat(String isbn, StockFormat format);
}
