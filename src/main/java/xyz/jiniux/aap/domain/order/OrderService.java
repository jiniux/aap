package xyz.jiniux.aap.domain.order;

import jakarta.persistence.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.billing.PaymentStrategy;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.order.events.OrderPlacedEvent;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.order.exceptions.OrderAlreadyConfirmedException;
import xyz.jiniux.aap.domain.order.exceptions.OrderNotFoundException;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.domain.warehouse.exceptions.NotEnoughItemsInStockException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;
import xyz.jiniux.aap.infrastructure.persistency.OrderRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final EntityManager entityManager;
    private final WarehouseService warehouseService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderService(WarehouseService warehouseService, OrderRepository orderRepository, ApplicationEventPublisher applicationEventPublisher, EntityManager entityManager) {
        this.warehouseService = warehouseService;
        this.orderRepository = orderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.entityManager = entityManager;
    }

    public void ensurePriceNotChanged(List<ShoppingCart.Item> shoppingCartItems) throws ItemsPriceChangedWhilePlacingOrderException {
        List<BigDecimal> pricesEur = warehouseService.getStockPricesEur(
                shoppingCartItems.stream().map(i -> new WarehouseService.GetStockPriceQuery(i.getIsbn(), i.getStockFormat(), i.getStockQuality())).toList());

        assert shoppingCartItems.size() == pricesEur.size();

        Iterator<BigDecimal> pricesIterator = pricesEur.iterator();
        Iterator<ShoppingCart.Item> itemsIterator = shoppingCartItems.iterator();

        List<ItemsPriceChangedWhilePlacingOrderException.Info> info = new ArrayList<>();
        while (pricesIterator.hasNext()) {
            BigDecimal priceEur = pricesIterator.next();
            ShoppingCart.Item item = itemsIterator.next();

            if (!priceEur.equals(item.getPriceEur())) {
                info.add(new ItemsPriceChangedWhilePlacingOrderException.Info(item.getIsbn(), item.getStockFormat(), item.getStockQuality(), item.getPriceEur(), priceEur));
            }
        }

        if (!info.isEmpty()) {
            throw new ItemsPriceChangedWhilePlacingOrderException(info);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Retryable(retryFor = { OptimisticLockException.class })
    public void confirmOrder(Order order) throws OrderNotFoundException, OrderAlreadyConfirmedException {
        try {
            entityManager.refresh(order, LockModeType.OPTIMISTIC);
        } catch (EntityNotFoundException e) {
            throw new OrderNotFoundException(order.getId());
        }

        if (order.isConfirmed()) {
            order.setConfirmed(true);
            order.setConfirmedAt(OffsetDateTime.now(ZoneOffset.UTC));
        } else {
            throw new OrderAlreadyConfirmedException(order.getId());
        }

        orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void placeOrderFromShoppingCartItems(String username, List<ShoppingCart.Item> shoppingCartItems, PaymentStrategy paymentStrategy, Address address)
            throws NotEnoughItemsInStockException, BookNotFoundException, StockNotOnSaleException, ItemsPriceChangedWhilePlacingOrderException
    {
        ensurePriceNotChanged(shoppingCartItems);

        BigDecimal finalPrice = BigDecimal.ZERO;

        for (ShoppingCart.Item item : shoppingCartItems) {
            this.warehouseService.reserveStock(item.getIsbn(), item.getStockFormat(), item.getStockQuality(), item.getQuantity());
            finalPrice = finalPrice.add(item.getPriceEur().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order();
        order.setItems(createOrderItems(shoppingCartItems));
        order.setUsername(username);
        order.setPlacedAt(OffsetDateTime.now(ZoneOffset.UTC));
        order.setFinalPrice(finalPrice);
        order.setAddress(address);

        orderRepository.save(order);

        applicationEventPublisher.publishEvent(new OrderPlacedEvent(order.getId(), order.getPlacedAt(), order.getUsername(), order.getFinalPrice(), paymentStrategy));
    }

    private static Set<Order.Item> createOrderItems(List<ShoppingCart.Item> items) {
        return items.stream().map(Order.Item::fromShoppingCartItem).collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Order findOrderById(Long orderId) throws OrderNotFoundException {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
