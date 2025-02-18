package xyz.jiniux.aap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "catalog_books")
@EqualsAndHashCode(of = "id")
public class CatalogBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 10000)
    private String description;

    @Column()
    private Integer publicationYear;

    @Column()
    private String edition;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId")
    List<BookFormatPreviewImage> formatPreviewImages;

    @ElementCollection
    @CollectionTable(name = "catalog_book_authors", joinColumns = @JoinColumn(name = "catalogBookId"))
    @Column(name = "authorId")
    private Set<Long> authorIds;

    @ManyToMany(
        cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
        fetch = FetchType.LAZY
    )
    @JoinTable(
        name = "catalog_book_authors",
        joinColumns = @JoinColumn(name = "catalogBookId"),
        inverseJoinColumns = @JoinColumn(name = "authorId"))
    private Set<Author> authors;

    @Column(nullable = false)
    private Long publisherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisherId", insertable = false, updatable = false)
    private Publisher publisher;

    @Version
    private Long version;

    @OneToMany(mappedBy = "bookId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Stock> stocks;
}
