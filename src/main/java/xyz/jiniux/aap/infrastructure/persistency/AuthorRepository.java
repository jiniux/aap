package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.model.Author;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("SELECT COUNT(e.id) FROM Author e WHERE e.id IN :ids")
    long countAllById(@Param("ids") Set<Long> ids);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT e.id FROM Author e WHERE e.id IN :ids")
    Set<Long> findAllIds(@Param("ids") Set<Long> ids);

    default boolean doAllAuthorsExist(Set<Long> ids) {
        Set<Long> existingIdsCount = findAllIds(ids);
        return existingIdsCount.size() == ids.size();
    }

    default Set<Long> getMissingAuthorIds(Set<Long> ids) {
        Set<Long> existingIds = findAllIds(ids);
        return new HashSet<>(CollectionUtils.disjunction(ids, existingIds));
    }
}
