package xyz.jiniux.aap.domain.order;

import jakarta.persistence.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.billing.PaymentStrategy;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.*;
import xyz.jiniux.aap.domain.order.events.OrderPlacedEvent;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;
import xyz.jiniux.aap.domain.order.exceptions.OrderAlreadyConfirmedException;
import xyz.jiniux.aap.domain.order.exceptions.OrderNotFoundException;
import xyz.jiniux.aap.domain.order.exceptions.ShipmentCostChangedException;
import xyz.jiniux.aap.domain.shipping.ShippingService;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.domain.warehouse.exceptions.NotEnoughItemsInStockException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;
import xyz.jiniux.aap.infrastructure.persistency.OrderRepository;
import xyz.jiniux.aap.infrastructure.persistency.ShoppingCartRepository;

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
    private final ShippingService shippingService;
    private final ShoppingCartRepository shoppingCartRepository;

    public OrderService(WarehouseService warehouseService, OrderRepository orderRepository, ApplicationEventPublisher applicationEventPublisher, EntityManager entityManager, ShippingService shippingService, ShoppingCartRepository shoppingCartRepository) {
        this.warehouseService = warehouseService;
        this.orderRepository = orderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.entityManager = entityManager;
        this.shippingService = shippingService;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    public void ensurePriceNotChanged(List<ShoppingCart.Item> shoppingCartItems, Map<String, List<Stock>> stocksByIsbn) throws ItemsPriceChangedWhilePlacingOrderException, StockNotOnSaleException {
        List<ItemsPriceChangedWhilePlacingOrderException.Info> info = new ArrayList<>();

        for (ShoppingCart.Item item : shoppingCartItems) {
            Stock stock = getStock(stocksByIsbn, item.getIsbn(), item.getStockFormat(), item.getStockQuality());

            if (!stock.getPriceEur().equals(item.getPriceEur())) {
                info.add(new ItemsPriceChangedWhilePlacingOrderException.Info(item.getIsbn(), item.getStockFormat(), item.getStockQuality(), item.getPriceEur(), stock.getPriceEur()));
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

    private static Stock getStock(Map<String, List<Stock>> stocks, String isbn, StockFormat stockFormat, StockQuality stockQuality) throws StockNotOnSaleException {
        List<Stock> selectedStocks = stocks.get(isbn);

        if (selectedStocks == null) {
            throw new StockNotOnSaleException(isbn, stockFormat, stockQuality);
        }

        return selectedStocks.stream()
                .filter(s -> s.getFormat().equals(stockFormat) && s.getQuality().equals(stockQuality))
                .findFirst()
                .orElseThrow(() -> new StockNotOnSaleException(isbn, stockFormat, stockQuality));
    }

    @Transactional(rollbackFor = Exception.class)
    public void placeOrderFromShoppingCartItems(
            String username,
            List<ShoppingCart.Item> shoppingCartItems,
            PaymentStrategy paymentStrategy,
            Address address,
            BigDecimal shipmentCost
    ) throws NotEnoughItemsInStockException,
            BookNotFoundException,
            StockNotOnSaleException,
            ItemsPriceChangedWhilePlacingOrderException,
            ShipmentCostChangedException
    {
        BigDecimal finalPrice = BigDecimal.ZERO;

        Set<String> isbns = shoppingCartItems.stream().map(ShoppingCart.Item::getIsbn).collect(Collectors.toSet());
        Map<String, List<Stock>> stocks = warehouseService.bulkGetStocksByISBNsForUpdate(isbns.stream().toList());

        ensurePriceNotChanged(shoppingCartItems, stocks);

        for (ShoppingCart.Item item : shoppingCartItems) {
            Stock stock = getStock(stocks, item.getIsbn(), item.getStockFormat(), item.getStockQuality());

            // No need to lock, it is already locked by bulkGetStocksByISBNsForUpdate
            this.warehouseService.reserveStock(stock, item.getQuantity(), false);

            finalPrice = finalPrice.add(item.getPriceEur().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal realShipmentCost = shippingService.calculateShipmentCosts(address);
        if (!shipmentCost.equals(realShipmentCost)) {
            throw new ShipmentCostChangedException(realShipmentCost);
        }

        finalPrice = finalPrice.add(realShipmentCost);

        Order order = new Order();
        order.setItems(createOrderItems(shoppingCartItems));
        order.setUsername(username);
        order.setPlacedAt(OffsetDateTime.now(ZoneOffset.UTC));
        order.setFinalPrice(finalPrice);
        order.setShipmentCost(realShipmentCost);
        order.setAddress(address);

        orderRepository.save(order);

        applicationEventPublisher.publishEvent(new OrderPlacedEvent(order.getId(), order.getPlacedAt(), order.getUsername(), order.getFinalPrice(), paymentStrategy));
    }

    public List<Order> getOrdersByUsername(String username) {
        return orderRepository.findAllByUsername(username);
    }

    private static Set<Order.Item> createOrderItems(List<ShoppingCart.Item> items) {
        return items.stream().map(Order.Item::fromShoppingCartItem).collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Order findOrderById(Long orderId) throws OrderNotFoundException {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
