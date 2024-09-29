package xyz.jiniux.aap.domain.cart;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.cart.exceptions.StocksQuantityNotAvailableException;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.infrastructure.persistency.ShoppingCartRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {
    private final WarehouseService warehouseService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final EntityManager entityManager;

    public ShoppingCartService(
        WarehouseService warehouseService,
        ShoppingCartRepository shoppingCartRepository,
        EntityManager entityManager
    ) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.warehouseService = warehouseService;
        this.entityManager = entityManager;
    }

    private void enforceStockAvailability(List<ShoppingCart.Item> items)
        throws StocksQuantityNotAvailableException
    {
        List<Integer> availabilities = this.warehouseService.checkStocksAvailability(
            items.stream().map(i -> new WarehouseService.CheckStockAvailabilityQuery(
                i.getIsbn(), i.getStockFormat(), i.getStockQuality()
            )).toList()
        );

        List<ImmutableTriple<String, StockFormat, StockQuality>> unavailableStocks = new ArrayList<>();
        for (int i = 0; i < items.size(); ++i) {
            boolean unavailable = availabilities.get(i) < items.get(i).getQuantity();

            if (unavailable) {
                ShoppingCart.Item item = items.get(i);
                unavailableStocks.add(
                    ImmutableTriple.of(item.getIsbn(), item.getStockFormat(), item.getStockQuality()));
            }
        }

        if (!unavailableStocks.isEmpty())
            throw new StocksQuantityNotAvailableException(unavailableStocks);
    }

    @Transactional
    public void pushShoppingCartUpdate(@NonNull String username, @NonNull List<ShoppingCart.Item> items)
        throws StocksQuantityNotAvailableException
    {
        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findCartByUsername(username);
        ShoppingCart shoppingCart;

        if (cartOptional.isPresent()) {
            shoppingCart = cartOptional.get();
            entityManager.lock(shoppingCart, LockModeType.OPTIMISTIC);
        } else {
            shoppingCart = ShoppingCart.createFor(username);
        }

        shoppingCart.setItems(items);
        enforceStockAvailability(shoppingCart.getItems());

        shoppingCartRepository.save(shoppingCart);
    }

    @Transactional
    public ShoppingCartSyncResult getSyncedShoppingCart(@NonNull String username)
    {
        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findCartByUsernameForUpdate(username);

        if (cartOptional.isEmpty())
            return new ShoppingCartSyncResult(ShoppingCart.createFor(username), Collections.emptyList());

        ShoppingCart shoppingCart = cartOptional.get();
        List<RemovedShoppingCartItem> removedItems = List.of();

        try {
            enforceStockAvailability(shoppingCart.getItems());
        } catch (StocksQuantityNotAvailableException e) {
            // save all removed items
            removedItems = shoppingCart.removeAllItem(
                e.getDetails().stream().map(m -> 
                    new ShoppingCart.ItemKey(m.getLeft(), m.getMiddle(), m.getRight())).toList()
            ).stream()
                .map(i -> new RemovedShoppingCartItem(i.getIsbn(), i.getStockFormat(), i.getStockQuality()))
                .toList();
        }

        shoppingCartRepository.save(shoppingCart);

        return new ShoppingCartSyncResult(shoppingCart, removedItems);
    }
}
