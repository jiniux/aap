package xyz.jiniux.aap.domain.shipping;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.ShipmentTracking;
import xyz.jiniux.aap.domain.model.ShipmentTrackingProgression;
import xyz.jiniux.aap.domain.shipping.exceptions.ShipmentTrackingAlreadyAddedToOrderException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ListIterator;

@Service
public class ShippingService {
    private final EntityManager entityManager;
    private final TrackingNumberAssigner trackingNumberAssigner;

    public ShippingService(EntityManager entityManager, TrackingNumberAssigner trackingNumberAssigner) {
        this.entityManager = entityManager;
        this.trackingNumberAssigner = trackingNumberAssigner;
    }

    @Retryable(retryFor = OptimisticLockException.class)
    public void addShipmentTrackingToOrder(Order order) throws ShipmentTrackingAlreadyAddedToOrderException {
        entityManager.refresh(order, LockModeType.OPTIMISTIC);

        if (order.getShipmentTracking() != null) {
            throw new ShipmentTrackingAlreadyAddedToOrderException(order.getId());
        }

        ShipmentTracking shipmentTracking = new ShipmentTracking();
        shipmentTracking.setTrackingNumber(trackingNumberAssigner.assignTrackingNumber(order));

        order.setShipmentTracking(shipmentTracking);

        entityManager.persist(order);
    }

    public BigDecimal calculateShipmentCosts(Address address) {
        if (address.country().equals("IT")) {
            return new BigDecimal("2.50");
        } else {
            return new BigDecimal("5.00");
        }
    }

    @Transactional
    @Retryable(retryFor = OptimisticLockException.class)
    public void registerTrackingProgression(ShipmentTracking shipmentTracking, ShipmentTrackingProgression newProgression) {
        entityManager.refresh(ShipmentTracking.class, LockModeType.OPTIMISTIC);

        ArrayList<ShipmentTrackingProgression> progressions = new ArrayList<>(shipmentTracking.getProgressions());
        ListIterator<ShipmentTrackingProgression> progressionIterator = progressions.listIterator();

        boolean added = false;

        while (progressionIterator.hasNext()) {
            ShipmentTrackingProgression progression = progressionIterator.next();

            if (progression.dateTime().isAfter(newProgression.dateTime())) {
                progressionIterator.previous();
                progressionIterator.add(newProgression);

                added = true;
            }
        }

        if (!added) {
            progressions.add(newProgression);
        }

        entityManager.persist(shipmentTracking);
    }
}
