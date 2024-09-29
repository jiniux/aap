package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.jiniux.aap.domain.catalog.exceptions.BookNotFoundException;
import xyz.jiniux.aap.domain.model.StockFormat;
import xyz.jiniux.aap.domain.model.StockQuality;
import xyz.jiniux.aap.domain.warehouse.WarehouseService;
import xyz.jiniux.aap.controllers.requests.FillStockRequest;
import xyz.jiniux.aap.controllers.requests.SetStockPriceRequest;
import xyz.jiniux.aap.mappers.StockFormatMapper;
import xyz.jiniux.aap.mappers.StockQualityMapper;
import xyz.jiniux.aap.validation.ValidStockFormat;
import xyz.jiniux.aap.validation.ValidStockQuality;

@RestController
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/fill")
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

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/put-on-sale")
    public ResponseEntity<?> setStockPrice(
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
            this.warehouseService.putStockOnSale(isbn, decodedStockFormat, decodedStockQuality);

            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        }
    }

    @PostMapping("/books/{isbn}/stocks/{stockFormat}/{stockQuality}/action/remove-from-sale")
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
        }
    }
}
