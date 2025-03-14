package xyz.jiniux.aap.infrastructure.persistency;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findForUpdate(@Param("orderId") int orderId);

    @Query("select o from Order o where o.username = :username order by o.placedAt desc")
    List<Order> findAllByUsername(@Param("username") String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.payment.id = :paymentId")
    Optional<Order> findOrderByPaymentIdForUpdate(@Param("paymentId") Long paymentId);
}
