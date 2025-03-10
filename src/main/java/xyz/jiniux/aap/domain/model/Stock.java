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

    @Column(nullable = false)
    private long quantity;

    public boolean isAvailable() {
        if (!isOnSale())
            return false;

        return getQuantity() > 0;
    }

    @Column
    private BigDecimal priceEur;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean onSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", insertable=false, updatable=false)
    private Book book;

    @Version
    private Long version;

    public static Stock createEmpty(long bookId, StockFormat format, StockQuality quality) {
        Stock stock = new Stock();
        stock.setBookId(bookId);
        stock.setFormat(format);
        stock.setQuality(quality);

        return stock;
    }

    public void addQuantity(long  quantity) {
        this.quantity += quantity;
    }

    public void removeQuantity(long quantity) {
        this.quantity -= quantity;
        assert this.quantity >= 0;
    }
}
