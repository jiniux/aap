package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.model.CatalogBook;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogBookRepository extends JpaRepository<CatalogBook, Integer> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT DISTINCT cb FROM CatalogBook cb JOIN cb.authors a " +
        "WHERE LOWER(cb.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
        "OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%'))" +
        "OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CatalogBook> searchCatalogBooks(String query, Pageable pageable);

    @Query("SELECT COUNT(cb) FROM CatalogBook cb JOIN cb.authors a WHERE a.id = :authorId")
    long countCatalogBookByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(cb) FROM CatalogBook cb WHERE cb.publisher.id = :publisherId")
    long countCatalogBookByPublisherId(@Param("publisherId") Long publisherId);

    Optional<CatalogBook> findCatalogBookByIsbn(String isbn);

    @Query("SELECT cb FROM CatalogBook cb")
    List<CatalogBook> searchCatalogBooks(Pageable pageable);
}
