package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.jiniux.aap.controllers.requests.ShipmentCostRequest;
import xyz.jiniux.aap.controllers.results.ShipmentCostResult;
import xyz.jiniux.aap.domain.model.Address;
import xyz.jiniux.aap.domain.shipping.ShippingService;
import xyz.jiniux.aap.mappers.AddressMapper;

import java.math.BigDecimal;

@RestController
public class ShippingController {
    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping(value = "/shipping/action/calculate-price")
    public ResponseEntity<?> calculatePrice(@RequestBody @Valid ShipmentCostRequest request) {
        Address address = AddressMapper.MAPPER.fromShipmentCostRequest(request);
        BigDecimal cost = shippingService.calculateShipmentCosts(address);

        return ResponseEntity.ok(new ShipmentCostResult(cost));
    }
}
