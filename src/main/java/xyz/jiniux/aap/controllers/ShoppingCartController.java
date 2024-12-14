package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.jiniux.aap.controllers.requests.SyncShoppingCartRequest;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResult;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultPriceChangedItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultRemovedItem;
import xyz.jiniux.aap.domain.cart.ShoppingCartService;
import xyz.jiniux.aap.domain.cart.ShoppingCartSyncResult;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.warehouse.exceptions.UnsupportedStockQualityException;
import xyz.jiniux.aap.mappers.*;

import java.security.Principal;
import java.util.List;

@RestController
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;

    public ShoppingCartController(ShoppingCartService shoppingCartService, OrderService orderService) {
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
    }

    @PostMapping(value = "/shopping-cart/sync")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<?> syncShoppingCart(Principal principal, @RequestBody @Valid SyncShoppingCartRequest request) {
        List<ShoppingCart.Item> items = CartItemMapper.MAPPER.fromSyncCartRequestItems(request.items());

        try {
            ShoppingCartSyncResult result = shoppingCartService.pushShoppingCartUpdate(principal.getName(), items, request.version());

            List<SyncShoppingCartResultItem> newItems = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
            List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
            List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

            return ResponseEntity.ok(new SyncShoppingCartResult(newItems, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
        } catch (UnsupportedStockQualityException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.createEbookOnlySupportsDigitalQuality());
        }
    }

    @GetMapping(value = "/shopping-cart/sync")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<?> getSyncedShoppingCart(Principal principal) {
        ShoppingCartSyncResult result = shoppingCartService.getSyncedShoppingCart(principal.getName());

        List<SyncShoppingCartResultItem> items = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromRemovedCartItems(result.removedItems());
        List<SyncShoppingCartResultPriceChangedItem> priceChangedItems = SyncShoppingCartResultPriceChangedItemMapper.MAPPER.fromPriceChangedCartItems(result.priceChangedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(items, removedItems, priceChangedItems, result.shoppingCart().getVersion()));
    }


}

