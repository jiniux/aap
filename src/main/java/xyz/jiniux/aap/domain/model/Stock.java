package xyz.jiniux.aap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Data
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = { "bookId", "format", "quality" })
})
@EqualsAndHashCode(of = {"bookId", "format", "quality"})
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private StockFormat format;

    @Column(nullable = false)
    private StockQuality quality;

    private Integer quantity;

    public boolean isAvailable() {
        if (!isOnSale())
            return false;

        if (getFormat() == StockFormat.EBOOK) {
            return true;
        }

        return getQuantity() > 0;
    }

    @Column(nullable = true)
    private BigDecimal priceEur;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean onSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", insertable=false, updatable=false)
    private CatalogBook book;

    @Version
    private Long version;

    public static Stock createEmpty(long bookId, StockFormat format, StockQuality quality) {
        Stock stock = new Stock();
        stock.setBookId(bookId);
        stock.setFormat(format);
        stock.setQuality(quality);

        return stock;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}
