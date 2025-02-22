package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.jiniux.aap.controllers.requests.CheckoutRequest;
import xyz.jiniux.aap.controllers.results.CheckoutResult;
import xyz.jiniux.aap.controllers.results.FullOrderResult;
import xyz.jiniux.aap.controllers.results.OrderSummariesResult;
import xyz.jiniux.aap.domain.billing.PaymentStrategy;
import xyz.jiniux.aap.domain.cart.ShoppingCartService;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.model.Order;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;
import xyz.jiniux.aap.domain.order.exceptions.OrderNotFoundException;
import xyz.jiniux.aap.domain.order.exceptions.ShipmentCostChangedException;
import xyz.jiniux.aap.domain.warehouse.exceptions.NotEnoughItemsInStockException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;
import xyz.jiniux.aap.mappers.*;

import java.security.Principal;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final ShoppingCartService shoppingCartService;

    public OrderController(OrderService orderService, ShoppingCartService shoppingCartService, JwtDecoder jwtDecoderByJwkKeySetUri) {
        this.orderService = orderService;
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping(value = "/orders/")
    @PreAuthorize("hasAuthority('view:orders')")
    public ResponseEntity<OrderSummariesResult> getOrderSummaries(Principal principal) {
        List<Order> orders = orderService.getOrdersByUsername(principal.getName());
        List<OrderSummariesResult.OrderSummary> summaries = OrderSummariesResultMapper.MAPPER.fromOrders(orders);

        return ResponseEntity.ok(new OrderSummariesResult(summaries));
    }

    @GetMapping(value = "/orders/{id}")
    @PreAuthorize("hasAuthority('view:orders')")
    public ResponseEntity<?> getOrder(Principal principal, @PathVariable Long id) {
        Order order;
        try {
            order = orderService.findOrderById(id);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponse.createOrderNotFound(e.getOrderId().toString()));
        }

        if (!order.getUsername().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ErrorResponse.createNoAccessToOrder(order.getId().toString()));
        }

        FullOrderResult orderResult = FullOrderResultMapper.MAPPER.fromOrder(order);
        return ResponseEntity.ok(orderResult);
    }

    @PostMapping(value = "/orders/action/place")
    @PreAuthorize("hasAuthority('place:order')")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> placeOrder(Principal principal, @RequestBody @Valid CheckoutRequest requestItems) {
        List<ShoppingCart.Item> items = CartItemMapper.MAPPER.fromCheckoutRequestItems(requestItems.items());
        PaymentStrategy paymentStrategy = requestItems.paymentStrategy().convert();
        Address address = AddressMapper.MAPPER.fromCheckoutRequestAddress(requestItems.address());

        try {
            orderService.placeOrderFromShoppingCartItems(principal.getName(), items, paymentStrategy, address, requestItems.shipmentCost());
            boolean cleared = shoppingCartService.clearShoppingCart(principal.getName(), items, requestItems.cartVersion());

            return ResponseEntity.ok(new CheckoutResult(cleared));
        } catch (NotEnoughItemsInStockException e) {
            String stockFormatString = StockFormatMapper.MAPPER.toString(e.getStockFormat());
            String stockQualityString = StockQualityMapper.MAPPER.toString(e.getStockQuality());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.createNotEnoughItemsInStock(e.getIsbn(), stockFormatString, stockQualityString));
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.createBookNotFound(e.getIsbn()));
        } catch (StockNotOnSaleException e) {
            String stockFormatString = StockFormatMapper.MAPPER.toString(e.getStockFormat());
            String stockQualityString = StockQualityMapper.MAPPER.toString(e.getStockQuality());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.createStockNotOnSale(e.getIsbn(), stockFormatString, stockQualityString));
        } catch (ItemsPriceChangedWhilePlacingOrderException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.createItemsPriceChanged(e.getInfo()));
        } catch (ShipmentCostChangedException e) {
            throw new RuntimeException(e);
        }
    }
}
