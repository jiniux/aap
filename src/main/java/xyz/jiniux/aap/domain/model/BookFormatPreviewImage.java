package xyz.jiniux.aap.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Blob;

@Entity
@Table(
    name = "stock_preview_images",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = { "bookId", "format" })
    }
)
@Data
public class BookFormatPreviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId", insertable = false, updatable = false)
    private Book book;

    @Column(nullable = false)
    private StockFormat format;

    @Lob
    @Column(nullable = false)
    private Blob image;
}
