package xyz.jiniux.aap.domain.checkout;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.billing.PaymentStrategy;
import xyz.jiniux.aap.domain.cart.ShoppingCartService;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;
import xyz.jiniux.aap.domain.order.exceptions.ShipmentCostChangedException;
import xyz.jiniux.aap.domain.warehouse.exceptions.NotEnoughItemsInStockException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutService {
    private final OrderService orderService;
    private final ShoppingCartService shoppingCartService;

    public CheckoutService(OrderService orderService, ShoppingCartService shoppingCartService) {
        this.orderService = orderService;
        this.shoppingCartService = shoppingCartService;
    }

    public record Result(boolean cartCleared) {}

    @Transactional
    public Result checkout(String username,
       List<ShoppingCart.Item> shoppingCartItems,
       long cartVersion,
       PaymentStrategy paymentStrategy,
       Address address,
       BigDecimal shipmentCost
    )
        throws
            ShipmentCostChangedException,
            NotEnoughItemsInStockException,
            BookNotFoundException,
            ItemsPriceChangedWhilePlacingOrderException,
            StockNotOnSaleException
    {
        orderService.placeOrderFromShoppingCartItems(username, shoppingCartItems, paymentStrategy, address, shipmentCost);
        boolean cartCleared = shoppingCartService.clearShoppingCart(username, cartVersion);

        return new Result(cartCleared);
    }
}
