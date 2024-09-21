package xyz.jiniux.aap.model;

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

    @Column(nullable = false)
    private String description;

    @Column()
    private Integer publicationYear;

    @Column()
    private String edition;

    @ElementCollection
    @CollectionTable(name = "catalog_book_authors", joinColumns = @JoinColumn(name = "catalogBookId"))
    @Column(name = "authorId")
    private Set<Long> authorIds;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "catalog_book_authors",
        joinColumns = @JoinColumn(name = "catalogBookId"),
        inverseJoinColumns = @JoinColumn(name = "authorId"))
    private Set<Author> authors;

    @Column(nullable = false)
    private Long publisherId;

    @ManyToOne
    @JoinColumn(name = "publisherId", insertable = false, updatable = false)
    private Publisher publisher;

    @Version
    private Long version;
}
