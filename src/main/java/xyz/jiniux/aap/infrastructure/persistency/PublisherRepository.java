package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.model.CatalogBook;
import xyz.jiniux.aap.model.Publisher;

import java.util.List;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    @Query("SELECT DISTINCT p FROM Publisher p " +
        "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.name asc")
    List<Publisher> searchPublishers(String query, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Publisher p ORDER BY p.name asc")
    List<Publisher> searchPublishers(Pageable pageable);
}
