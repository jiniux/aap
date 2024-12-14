package xyz.jiniux.aap.domain.accounting;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import xyz.jiniux.aap.domain.accounting.events.PaymentInitiatedEvent;
import xyz.jiniux.aap.domain.accounting.exceptions.PaymentNotFoundException;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.order.events.OrderPlacedEvent;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.Payment;
import xyz.jiniux.aap.domain.order.exceptions.OrderNotFoundException;
import xyz.jiniux.aap.infrastructure.persistency.OrderRepository;

@Component
public class OrderPaymentSaga {
    private final AccountingService accountingService;
    private final OrderService orderService;

    public OrderPaymentSaga(AccountingService accountingService, OrderRepository orderRepository, OrderService orderService) {
        this.accountingService = accountingService;
        this.orderService = orderService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        Payment payment = accountingService.initiatePayment(event.paymentStrategy(), event.finalPrice());

        Order order;
        try {
            order = orderService.findOrderById(event.orderId());
        } catch (OrderNotFoundException e) {
            throw new RuntimeException(e);
        }

        order.setPayment(payment);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePaymentStateInitiateEvent(PaymentInitiatedEvent event) {
        Payment payment;
        try {
            payment = accountingService.findPaymentById(event.paymentId());
        } catch (PaymentNotFoundException e) {
            throw new RuntimeException(e);
        }

        accountingService.executePayment(payment, event.paymentStrategy());
    }
}
