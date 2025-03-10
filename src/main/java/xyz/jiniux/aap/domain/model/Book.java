package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "books")
@EqualsAndHashCode(of = "id")
public class Book {
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

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Access(AccessType.FIELD)
    private Set<BookCategory> categories;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookId")
    List<BookFormatPreviewImage> formatPreviewImages;

    @ElementCollection
    @CollectionTable(name = "book_authors", joinColumns = @JoinColumn(name = "bookId"))
    @Column(name = "authorId")
    private Set<Long> authorIds;

    @ManyToMany(
        cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
        fetch = FetchType.LAZY
    )
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "bookId"),
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
