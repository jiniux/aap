package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.Stock;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.controllers.requests.FillStockRequest;
import xyz.jiniux.aap.controllers.requests.SetStockPriceRequest;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockAlreadyOnSaleException;
import xyz.jiniux.aap.domain.warehouse.exceptions.StockNotOnSaleException;
import xyz.jiniux.aap.domain.warehouse.exceptions.UnsupportedStockQualityException;
import xyz.jiniux.aap.mappers.StockFormatMapper;
import xyz.jiniux.aap.mappers.StockQualityMapper;
import xyz.jiniux.aap.mappers.StockResultElementMapper;
import xyz.jiniux.aap.validation.ValidStockFormat;
import xyz.jiniux.aap.validation.ValidStockQuality;

import java.util.List;

@RestController
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/fill")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> restockBook(
        @PathVariable("isbn") @ISBN String isbn,
        @PathVariable("stockFormat") @ValidStockFormat String stockFormat,
        @PathVariable("stockQuality") @ValidStockQuality String stockQuality,
        @RequestBody @Valid FillStockRequest request
    ) {
        StockFormat decodedStockFormat = StockFormatMapper.MAPPER.fromString(stockFormat);
        StockQuality decodedStockQuality = StockQualityMapper.MAPPER.fromString(stockQuality);

        if (decodedStockFormat == StockFormat.EBOOK && decodedStockQuality != StockQuality.DIGITAL)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createEbookOnlySupportsDigitalQuality()
            );

        try {
            this.warehouseService.refillStock(isbn, decodedStockFormat, decodedStockQuality, request.quantity());

            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        }
    }

    @PatchMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/price")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> setStockPrice(
        @PathVariable("isbn") @ISBN String isbn,
        @PathVariable("stockFormat") @ValidStockFormat String stockFormat,
        @PathVariable("stockQuality") @ValidStockQuality String stockQuality,
        @RequestBody @Valid SetStockPriceRequest request
    ) {
        StockFormat decodedStockFormat = StockFormatMapper.MAPPER.fromString(stockFormat);
        StockQuality decodedStockQuality = StockQualityMapper.MAPPER.fromString(stockQuality);

        if (decodedStockFormat == StockFormat.EBOOK && decodedStockQuality != StockQuality.DIGITAL)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createEbookOnlySupportsDigitalQuality()
            );

        try {
            this.warehouseService.setStockPrice(isbn, decodedStockFormat, decodedStockQuality, request.priceEur());

            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        }
    }

    @GetMapping("/books/{isbn}/stocks")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> getStocks(@PathVariable("isbn") @ISBN String isbn) {
        List<Stock> stocks = this.warehouseService.getAvailableStocksByIsbn(isbn);

        return ResponseEntity.ok(StockResultElementMapper.MAPPER.fromStocks(stocks));
    }

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/put-on-sale")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> setStockPrice(
        @PathVariable("isbn") @ISBN String isbn,
        @PathVariable("stockFormat") @ValidStockFormat String stockFormat,
        @PathVariable("stockQuality") @ValidStockQuality String stockQuality
    ) {
        StockFormat decodedStockFormat = StockFormatMapper.MAPPER.fromString(stockFormat);
        StockQuality decodedStockQuality = StockQualityMapper.MAPPER.fromString(stockQuality);

        try {
            this.warehouseService.putStockOnSale(isbn, decodedStockFormat, decodedStockQuality);
            return ResponseEntity.ok().build();
        } catch (UnsupportedStockQualityException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createEbookOnlySupportsDigitalQuality()
            );
        }
        catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        } catch (StockAlreadyOnSaleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createStockAlreadyOnSale(e.getIsbn(), stockFormat, stockQuality)
            );
        }
    }

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/remove-from-sale")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> removeFromSale(
        @PathVariable("isbn") @ISBN String isbn,
        @PathVariable("stockFormat") @ValidStockFormat String stockFormat,
        @PathVariable("stockQuality") @ValidStockQuality String stockQuality
    ) {
        StockFormat decodedStockFormat = StockFormatMapper.MAPPER.fromString(stockFormat);
        StockQuality decodedStockQuality = StockQualityMapper.MAPPER.fromString(stockQuality);

        if (decodedStockFormat == StockFormat.EBOOK && decodedStockQuality != StockQuality.DIGITAL)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createEbookOnlySupportsDigitalQuality()
            );

        try {
            this.warehouseService.removeStockFromSale(isbn, decodedStockFormat, decodedStockQuality);

            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        } catch (StockNotOnSaleException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createStockNotOnSale(e.getIsbn(), stockFormat, stockQuality)
            );
        }
    }
}
