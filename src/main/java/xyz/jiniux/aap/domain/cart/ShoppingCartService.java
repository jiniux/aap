package xyz.jiniux.aap.domain.cart;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.cart.exceptions.StocksQuantityNotAvailableException;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.domain.warehouse.exceptions.UnsupportedStockQualityException;
import xyz.jiniux.aap.infrastructure.persistency.ShoppingCartRepository;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ShoppingCartService {
    private final WarehouseService warehouseService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final EntityManager entityManager;

    public ShoppingCartService(
        WarehouseService warehouseService,
        ShoppingCartRepository shoppingCartRepository,
        EntityManager entityManager)
    {
        this.shoppingCartRepository = shoppingCartRepository;
        this.warehouseService = warehouseService;
        this.entityManager = entityManager;
    }

    private void enforceStockAvailability(List<ShoppingCart.Item> items)
        throws StocksQuantityNotAvailableException
    {
        List<Long> availabilities = this.warehouseService.checkStocksAvailability(
            items.stream().map(i -> new WarehouseService.CheckStockAvailabilityQuery(
                i.getIsbn(), i.getStockFormat(), i.getStockQuality()
            )).toList()
        );

        List<ImmutableTriple<String, StockFormat, StockQuality>> unavailableStocks = checkUnavailableStocks(items, availabilities);

        if (!unavailableStocks.isEmpty())
            throw new StocksQuantityNotAvailableException(unavailableStocks);
    }

    private static List<ImmutableTriple<String, StockFormat, StockQuality>> checkUnavailableStocks(List<ShoppingCart.Item> items, List<Long> availabilities) {
        List<ImmutableTriple<String, StockFormat, StockQuality>> unavailableStocks = new ArrayList<>();
        for (int i = 0; i < items.size(); ++i) {
            boolean unavailable = availabilities.get(i) < items.get(i).getQuantity();

            if (unavailable) {
                ShoppingCart.Item item = items.get(i);
                unavailableStocks.add(
                    ImmutableTriple.of(item.getIsbn(), item.getStockFormat(), item.getStockQuality()));
            }
        }
        return unavailableStocks;
    }

    private List<PriceChangedShoppingCartItem> fillPrices(List<ShoppingCart.Item> items) {
        List<BigDecimal> pricesEur = warehouseService.getStockPricesEur(items.stream().map(i -> new WarehouseService.GetStockPriceQuery(i.getIsbn(), i.getStockFormat(), i.getStockQuality())).toList());

        Iterator<BigDecimal> pricesIterator = pricesEur.iterator();
        Iterator<ShoppingCart.Item> itemsIterator = items.iterator();

        List<PriceChangedShoppingCartItem> results = new ArrayList<>();

        while (pricesIterator.hasNext()) {
            BigDecimal priceEur = pricesIterator.next();
            ShoppingCart.Item item = itemsIterator.next();

            boolean priceChanged = item.getPriceEur() != null && !priceEur.equals(item.getPriceEur());
            if (priceChanged) {
                results.add(new PriceChangedShoppingCartItem(item.getIsbn(), item.getStockFormat(), item.getStockQuality(), priceEur));
            }

            item.setPriceEur(priceEur);
        }

        return results;
    }

    private void enforceValidStocks(List<ShoppingCart.Item> items) throws UnsupportedStockQualityException {
        for (ShoppingCart.Item item: items) {
            warehouseService.enforceValidStock(item.getIsbn(), item.getStockFormat(), item.getStockQuality());
        }
    }

    @Transactional
    public ShoppingCartSyncResult pushShoppingCartUpdate(@NonNull String username, @NonNull List<ShoppingCart.Item> items, long cartVersion)
            throws UnsupportedStockQualityException
    {
        enforceValidStocks(items);

        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findCartByUsername(username);
        ShoppingCart shoppingCart;

        if (cartOptional.isPresent()) {
            shoppingCart = cartOptional.get();

            if (shoppingCart.getVersion() != cartVersion) {
                throw new OptimisticLockException("Shopping cart version mismatch (expected: " + cartVersion + ", actual: " + shoppingCart.getVersion() + ")");
            }

            entityManager.lock(shoppingCart, LockModeType.OPTIMISTIC);
        } else {
            shoppingCart = ShoppingCart.createFor(username);
        }

        List<RemovedShoppingCartItem> removedItems = removeUnavailableStocks(items, shoppingCart);
        List<PriceChangedShoppingCartItem> priceChangedShoppingCartItems = fillPrices(items);

        shoppingCart.setItems(items);

        shoppingCartRepository.save(shoppingCart);

        return new ShoppingCartSyncResult(shoppingCart, removedItems, priceChangedShoppingCartItems);
    }

    private List<RemovedShoppingCartItem> removeUnavailableStocks(List<ShoppingCart.Item> items, ShoppingCart shoppingCart) {
        try {
            enforceStockAvailability(items);
        } catch (StocksQuantityNotAvailableException e) {
            return shoppingCart.removeAllItems(
                            e.getDetails().stream().map(m ->
                                    new ShoppingCart.ItemKey(m.getLeft(), m.getMiddle(), m.getRight())).toList()
                    ).stream()
                .map(i -> new RemovedShoppingCartItem(i.getIsbn(), i.getStockFormat(), i.getStockQuality()))
                .toList();
        }

        return Collections.emptyList();
    }

    @Transactional
    public ShoppingCartSyncResult getSyncedShoppingCart(@NonNull String username)
    {
        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findCartByUsernameForUpdate(username);

        if (cartOptional.isEmpty())
            return new ShoppingCartSyncResult(ShoppingCart.createFor(username), Collections.emptyList(), Collections.emptyList());

        ShoppingCart shoppingCart = cartOptional.get();
        List<RemovedShoppingCartItem> removedItems = removeUnavailableStocks(shoppingCart.getItems(), shoppingCart);

        List<PriceChangedShoppingCartItem> priceChangedShoppingCartItems = fillPrices(shoppingCart.getItems());

        shoppingCartRepository.save(shoppingCart);

        return new ShoppingCartSyncResult(shoppingCart, removedItems, priceChangedShoppingCartItems);
    }
}
