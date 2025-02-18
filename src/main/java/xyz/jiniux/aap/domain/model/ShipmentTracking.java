package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment_trackings")
@Data
public class ShipmentTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trackingNumber;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    @Access(AccessType.PROPERTY)
    private List<ShipmentTrackingProgression> progressions;

    public List<ShipmentTrackingProgression> getProgressions() {
        return List.copyOf(progressions);
    }

    public void setProgressions(List<ShipmentTrackingProgression> progressions) {
        this.progressions = new ArrayList<>(progressions);
    }

    @Version
    private Long version;
}
