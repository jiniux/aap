package xyz.jiniux.aap.domain.warehouse;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import xyz.jiniux.aap.domain.billing.events.PaymentStateChangeEvent;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.PaymentState;
import xyz.jiniux.aap.infrastructure.persistency.OrderRepository;

import java.util.Optional;

@Component
public class RestoreQuantityEventListener {
    private final OrderRepository orderRepository;
    private final WarehouseService warehouseService;

    public RestoreQuantityEventListener(OrderRepository orderRepository, WarehouseService warehouseService) {
        this.orderRepository = orderRepository;
        this.warehouseService = warehouseService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restoreQuantityOnPaymentFailed(PaymentStateChangeEvent event) {
        if (event.state().isErrored()) {
            Optional<Order> optionalOrder = orderRepository.findOrderByPaymentIdForUpdate(event.paymentId());

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();

                for (Order.Item item : order.getItems()) {
                    try {
                        this.warehouseService.refillStock(
                                item.getIsbn(),
                                item.getStockFormat(),
                                item.getStockQuality(),
                                item.getQuantity()
                        );
                    } catch (Exception e) {
                        System.out.println("Failed to restore quantity for order item: " + item);
                    }
                }
            }
        }
    }
}
