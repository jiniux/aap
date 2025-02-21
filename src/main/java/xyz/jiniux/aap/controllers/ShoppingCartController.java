package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.jiniux.aap.controllers.requests.AddItemShoppingCartRequest;
import xyz.jiniux.aap.controllers.requests.RemoveItemShoppingCartRequest;
import xyz.jiniux.aap.controllers.requests.UpdateItemShoppingCartRequest;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResult;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultPriceChangedItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultRemovedItem;
import xyz.jiniux.aap.domain.cart.ShoppingCartService;
import xyz.jiniux.aap.domain.cart.ShoppingCartSyncResult;
import xyz.jiniux.aap.domain.cart.ShoppingCartUpdate;
import xyz.jiniux.aap.domain.cart.exceptions.ItemNotFoundInCartException;
import xyz.jiniux.aap.domain.cart.exceptions.StocksQuantityNotAvailableException;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.mappers.*;

import java.security.Principal;
import java.util.List;

@RestController
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping(value = "/shopping-cart/action/add-item")
    @PreAuthorize("hasAuthority('sync:shopping-cart')")
    public ResponseEntity<?> addItemToCart(Principal principal, @RequestBody @Valid AddItemShoppingCartRequest request) {
        ShoppingCartUpdate.AddItem addItem = ShoppingCartUpdateMapper.MAPPER.fromAddItemRequestItem(request.item());

        ShoppingCartSyncResult result = null;
        try {
            result = shoppingCartService.pushShoppingCartUpdate(principal.getName(), addItem);
        } catch (StocksQuantityNotAvailableException e) {
            ImmutableTriple<String, StockFormat, StockQuality> notAvailableItem = e.getDetails().getFirst();

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ErrorResponse.createStockQuantityNotAvailable(
                            notAvailableItem.getLeft(),
                            StockFormatMapper.MAPPER.toString(notAvailableItem.getMiddle()),
                            StockQualityMapper.MAPPER.toString(notAvailableItem.getRight())
                    )
            );
        } catch (ItemNotFoundInCartException e) {
            // should never happen on add item
            throw new IllegalStateException("Unexpected item not found in cart exception on add item");
        }

        List<SyncShoppingCartResultItem> newItems = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
        List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(newItems, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
    }

    @PostMapping(value = "/shopping-cart/action/remove-item")
    @PreAuthorize("hasAuthority('sync:shopping-cart')")
    public ResponseEntity<?> removeItemFromShoppingCart(Principal principal, @RequestBody @Valid RemoveItemShoppingCartRequest request) {
        ShoppingCartUpdate.RemoveItem removeItem = ShoppingCartUpdateMapper.MAPPER.fromRemoveItemRequestItem(request.item());

        ShoppingCartSyncResult result = null;
        try {
            result = shoppingCartService.pushShoppingCartUpdate(principal.getName(), removeItem);
        } catch (StocksQuantityNotAvailableException | ItemNotFoundInCartException e) {
            // should never happen on remove item
            throw new IllegalStateException("Unexpected stock quantity not available exception on remove item");
        }

        List<SyncShoppingCartResultItem> newItems = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
        List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(newItems, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
    }

    @PostMapping(value = "/shopping-cart/action/edit-item")
    @PreAuthorize("hasAuthority('sync:shopping-cart')")
    public ResponseEntity<?> updateItem(Principal principal, @RequestBody @Valid UpdateItemShoppingCartRequest request) {
        ShoppingCartUpdate.UpdateItem updateItem = ShoppingCartUpdateMapper.MAPPER.fromUpdateItemRequestItem(request.item());

        ShoppingCartSyncResult result = null;
        try {
            result = shoppingCartService.pushShoppingCartUpdate(principal.getName(), updateItem);
        } catch (StocksQuantityNotAvailableException e) {
            ImmutableTriple<String, StockFormat, StockQuality> notAvailableItem = e.getDetails().getFirst();

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ErrorResponse.createStockQuantityNotAvailable(
                            notAvailableItem.getLeft(),
                            StockFormatMapper.MAPPER.toString(notAvailableItem.getMiddle()),
                            StockQualityMapper.MAPPER.toString(notAvailableItem.getRight())
                    )
            );
        } catch (ItemNotFoundInCartException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ErrorResponse.createStockQuantityNotAvailable(
                            e.getIsbn(),
                            StockFormatMapper.MAPPER.toString(e.getStockFormat()),
                            StockQualityMapper.MAPPER.toString(e.getStockQuality())
                    )
            );
        }

        List<SyncShoppingCartResultItem> newItems = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
        List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(newItems, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
    }

    @GetMapping(value = "/shopping-cart/sync")
    @PreAuthorize("hasAuthority('sync:shopping-cart')")
    public ResponseEntity<?> getSyncedShoppingCart(Principal principal) {
        ShoppingCartSyncResult result = shoppingCartService.getSyncedShoppingCart(principal.getName());

        List<SyncShoppingCartResultItem> items = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
        List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(items, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
    }


}

