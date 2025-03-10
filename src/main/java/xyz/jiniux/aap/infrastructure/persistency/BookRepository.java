package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.domain.model.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT DISTINCT cb FROM Book cb JOIN cb.authors a " +
        "WHERE LOWER(cb.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%'))" +
        "OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(cb) FROM Book cb JOIN cb.authors a WHERE a.id = :authorId")
    long countBookByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(cb) FROM Book cb WHERE cb.publisher.id = :publisherId")
    long countBookByPublisherId(@Param("publisherId") Long publisherId);

    Optional<Book> findBookByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select cb from Book cb where cb.isbn = :isbn")
    Optional<Book> findBookByIsbnForShare(@Param("isbn") String isbn);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("SELECT cb FROM Book cb WHERE cb.isbn = :isbn")
    Optional<Book> findBookByIsbnForUpdate(String isbn);

    @Query("SELECT cb FROM Book cb")
    List<Book> searchBooks(Pageable pageable);
}
