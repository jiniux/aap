package xyz.jiniux.aap.domain.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

public record ShipmentTrackingProgression(OffsetDateTime dateTime, ShipmentTrackingProgressionState state)
        implements Serializable { }
