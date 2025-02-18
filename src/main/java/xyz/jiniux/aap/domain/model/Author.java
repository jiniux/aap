package xyz.jiniux.aap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "authors")
@EqualsAndHashCode(of = "id")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Lob
    @Column()
    private byte[] picture;

    @ManyToMany
    @JoinTable(
        name = "catalog_book_authors",
        joinColumns = @JoinColumn(name = "authorId"),
        inverseJoinColumns = @JoinColumn(name = "catalogBookId"))
    private List<CatalogBook> books;

    @Version
    private Long version;
}
