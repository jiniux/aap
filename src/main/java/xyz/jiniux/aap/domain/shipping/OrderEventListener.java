package xyz.jiniux.aap.domain.shipping;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.order.events.OrderConfirmedEvent;

@Component
public class OrderEventListener {
    private final ShippingService shippingService;
    private final OrderService orderService;

    public OrderEventListener(ShippingService shippingService, OrderService orderService) {
        this.shippingService = shippingService;
        this.orderService = orderService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        Order order;
        try {
            order = orderService.findOrderById(event.orderId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            shippingService.addShipmentTrackingToOrder(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
