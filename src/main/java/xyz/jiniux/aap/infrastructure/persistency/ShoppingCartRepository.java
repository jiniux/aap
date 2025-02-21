package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.jiniux.aap.domain.model.ShoppingCart;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findCartByUsername(@NonNull String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ShoppingCart c where c.username = :username")
    Optional<ShoppingCart> findCartByUsernameForUpdate(@Param("username") @NonNull String username);
}
