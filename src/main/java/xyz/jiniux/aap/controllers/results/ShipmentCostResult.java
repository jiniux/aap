
package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record ShipmentCostResult(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal costEur
) {}
