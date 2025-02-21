package xyz.jiniux.aap.domain.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.billing.events.PaymentInitiatedEvent;
import xyz.jiniux.aap.domain.billing.events.PaymentStateChangeEvent;
import xyz.jiniux.aap.domain.billing.exceptions.PaymentNotFoundException;
import xyz.jiniux.aap.domain.model.Payment;
import xyz.jiniux.aap.domain.model.PaymentState;
import xyz.jiniux.aap.infrastructure.persistency.PaymentRepository;

import java.math.BigDecimal;

@Service
public class BillingService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentRepository paymentRepository;
    private final EntityManager entityManager;

    public BillingService(ApplicationEventPublisher applicationEventPublisher, PaymentRepository paymentRepository,
                          EntityManager entityManager) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.paymentRepository = paymentRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Payment initiatePayment(PaymentStrategy paymentStrategy, BigDecimal amount) {
        Payment payment = new Payment();

        payment.setAmount(amount);
        payment.setState(PaymentState.PENDING);
        payment.setMethod(paymentStrategy.getMethod());
        payment.setAdditionalInfo(paymentStrategy.getAdditionalInfo());

        paymentRepository.save(payment);

        applicationEventPublisher.publishEvent(new PaymentInitiatedEvent(payment.getId(), amount, paymentStrategy));
        applicationEventPublisher.publishEvent(PaymentStateChangeEvent.fromPayment(payment));

        return payment;
    }

    public Payment findPaymentById(Long id) throws PaymentNotFoundException {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Transactional
    public void executePayment(Payment payment, PaymentStrategy paymentStrategy) {
        entityManager.refresh(payment, LockModeType.PESSIMISTIC_WRITE);

        // Already processed, just ignore
        if (payment.getState() != PaymentState.PENDING)
        {
            return;
        }

        if (payment.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            PaymentState newState = paymentStrategy.execute(payment.getAmount()).toPaymentState();
            payment.setState(newState);

            if (newState == PaymentState.ERRORED_UNKNOWN || newState == PaymentState.ERRORED_NOT_ENOUGH_FUNDS) {
                applicationEventPublisher.publishEvent(PaymentStateChangeEvent.fromPayment(payment));
            }
        } else {
            payment.setState(PaymentState.COMPLETED);
        }

        paymentRepository.save(payment);

        applicationEventPublisher.publishEvent(PaymentStateChangeEvent.fromPayment(payment));
    }
}
