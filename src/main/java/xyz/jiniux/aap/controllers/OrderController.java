package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.jiniux.aap.controllers.requests.CheckoutRequest;
import xyz.jiniux.aap.domain.accounting.PaymentStrategy;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.model.ShoppingCart;
import xyz.jiniux.aap.domain.order.OrderService;
import xyz.jiniux.aap.domain.order.exceptions.ItemsPriceChangedWhilePlacingOrderException;
import xyz.jiniux.aap.domain.warehouse.exceptions.NotEnoughItemsInStockException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;
import xyz.jiniux.aap.mappers.AddressMapper;
import xyz.jiniux.aap.mappers.CartItemMapper;
import xyz.jiniux.aap.mappers.StockFormatMapper;
import xyz.jiniux.aap.mappers.StockQualityMapper;

import java.security.Principal;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "/order/place")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<?> placeOrder(Principal principal, @RequestBody @Valid CheckoutRequest requestItems) {
        List<ShoppingCart.Item> items = CartItemMapper.MAPPER.fromCheckoutRequestItems(requestItems.items());
        PaymentStrategy paymentStrategy = requestItems.paymentStrategy().convert();
        Address address = AddressMapper.MAPPER.fromCheckoutRequestAddress(requestItems.address());

        try {
            orderService.placeOrderFromShoppingCartItems(principal.getName(), items, paymentStrategy, address);

            return ResponseEntity.ok().build();
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
        }
    }
}
