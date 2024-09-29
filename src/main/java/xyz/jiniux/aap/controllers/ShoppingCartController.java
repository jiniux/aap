package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.jiniux.aap.controllers.requests.SyncShoppingCartRequestItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResult;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultItem;
import xyz.jiniux.aap.controllers.results.SyncShoppingCartResultRemovedItem;
import xyz.jiniux.aap.domain.cart.ShoppingCartService;
import xyz.jiniux.aap.domain.cart.ShoppingCartSyncResult;
import xyz.jiniux.aap.domain.cart.exceptions.StocksQuantityNotAvailableException;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.mappers.CartItemMapper;
import xyz.jiniux.aap.mappers.SyncShoppingCartResultItemMapper;
import xyz.jiniux.aap.mappers.SyncShoppingCartResultRemovedItemMapper;

import java.security.Principal;
import java.util.List;

@RestController
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping(value = "/shopping-cart/sync")
    public ResponseEntity<?> syncShoppingCart(Principal principal, @RequestBody @Valid List<SyncShoppingCartRequestItem> requestItems) {
        List<ShoppingCart.Item> items = CartItemMapper.MAPPER.fromSyncCartRequestItems(requestItems);

        if (items.stream().anyMatch(i -> i.getStockFormat() == StockFormat.EBOOK && i.getStockQuality() != StockQuality.DIGITAL))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.createEbookOnlySupportsDigitalQuality());

        try {
            shoppingCartService.pushShoppingCartUpdate(principal.getName(), items);
            return ResponseEntity.ok().build();
        } catch (StocksQuantityNotAvailableException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.createStocksNotAvailable(e.getDetails()));
        }
    }

    @GetMapping(value = "/shopping-cart/sync")
    public ResponseEntity<?> getSyncedShoppingCart(Principal principal) {
        ShoppingCartSyncResult result = shoppingCartService.getSyncedShoppingCart(principal.getName());

        List<SyncShoppingCartResultItem> items = SyncShoppingCartResultItemMapper.MAPPER.fromCartItems(result.shoppingCart().getItems());
        List<SyncShoppingCartResultRemovedItem> removedItems = SyncShoppingCartResultRemovedItemMapper.MAPPER.fromCartItems(result.removedItems());

        return ResponseEntity.ok(new SyncShoppingCartResult(items, removedItems));
    }
}

