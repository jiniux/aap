package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "orders", indexes = { @Index(columnList = "username") })
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime placedAt;

    @Setter
    @Getter
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private BigDecimal finalPrice;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private ShipmentTracking shipmentTracking;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Payment payment;

    @Column(nullable = false)
    private boolean confirmed;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride( name = "country", column = @Column(name = "address_country", nullable = false)),
        @AttributeOverride( name = "state", column = @Column(name = "address_state", nullable = false)),
        @AttributeOverride( name = "city", column = @Column(name = "address_city", nullable = false)),
        @AttributeOverride( name = "street", column = @Column(name = "address_street", nullable = false)),
        @AttributeOverride( name = "number", column = @Column(name = "address_number", nullable = false)),
        @AttributeOverride( name = "zipCode", column = @Column(name = "address_zip_code", nullable = false)),
        @AttributeOverride( name = "recipientName", column = @Column(name = "address_recipient_name", nullable = false)),
    })
    private Address address;

    private OffsetDateTime confirmedAt;

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"isbn", "stockFormat", "stockQuality"})
    public static class Item implements Serializable
    {
        private final String isbn;
        private final StockFormat stockFormat;
        private final StockQuality stockQuality;
        private final long quantity;

        public static Item fromShoppingCartItem(ShoppingCart.Item item) {
            return new Item(item.getIsbn(), item.getStockFormat(), item.getStockQuality(), item.getQuantity());
        }
    }

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    @Getter
    private Set<Item> items;

    @Version
    private Long version;
}

