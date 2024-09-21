package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.model.Publisher;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select p from Publisher p where p.id = :id")
    Publisher findByIdOptimistic(Long id);
}
