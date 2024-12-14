package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.jiniux.aap.controllers.requests.*;
import xyz.jiniux.aap.controllers.results.*;
import xyz.jiniux.aap.domain.catalog.*;
import xyz.jiniux.aap.domain.catalog.exceptions.*;
import xyz.jiniux.aap.mappers.*;
import xyz.jiniux.aap.domain.model.Author;
import xyz.jiniux.aap.domain.model.CatalogBook;
import xyz.jiniux.aap.domain.model.Publisher;
import xyz.jiniux.aap.support.ISBNCleaner;
import xyz.jiniux.aap.validation.ValidAuthorId;
import xyz.jiniux.aap.validation.ValidPublisherId;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping(value = "/books")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> registerBook(@RequestBody @Valid BookRegistrationRequest request)
    {
        try {
            this.catalogService.registerBook(CatalogBookMapper.MAPPER.fromBookRegistrationRequest(request));
            return ResponseEntity.created(URI.create("/books/" + ISBNCleaner.clean(request.getIsbn()))).build();
        } catch (AuthorsNotFoundException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createAuthorsNotFound(e.getIds().stream().map(Object::toString).collect(Collectors.toSet()))
            );
        } catch (PublisherNotFoundException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createPublisherNotFound(Long.toString(e.getPublisherId()))
            );
        } catch (ISBNAlreadyRegisteredException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createIsbnAlreadyRegistered(e.getIsbn())
            );
        } catch (NoAuthorSpecifiedException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createNoAuthorSpecified()
            );
        }
    }

    private static final int BOOK_SEARCH_MAX_PAGE_SIZE = 50;

    @GetMapping(value = "/books")
    public ResponseEntity<List<BookSearchResultEntry>> searchBooks(
        @RequestParam(name = "query", required = false) @NotNull String query,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "pageSize", required = false) Integer pageSize)
    {
        pageSize = pageSize == null ? BOOK_SEARCH_MAX_PAGE_SIZE : Math.min(BOOK_SEARCH_MAX_PAGE_SIZE, pageSize);

        List<CatalogBook> books = this.catalogService.searchBooks(query.trim(), pageSize, page);

        return ResponseEntity.ok(BookSearchResultEntryMapper.MAPPER.fromCatalogBooks(books));
    }

    // Use patch because the user does not need to specify all the fields of the book
    @PatchMapping(value = "/books/{isbn}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> editBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn,
        @Valid @RequestBody EditBookRequest request
    )
    {
        try {
            this.catalogService.editBook(
                ISBNCleaner.clean(isbn),
                PartialCatalogBookMapper.MAPPER.fromEditBookRequest(request)
            );

            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        } catch (AuthorsNotFoundException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createAuthorsNotFound(e.getIds().stream().map(Object::toString).collect(Collectors.toSet()))
            );
        } catch (PublisherNotFoundException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createPublisherNotFound(Long.toString(e.getPublisherId()))
            );
        } catch (NoAuthorSpecifiedException e) {
            return ResponseEntity.badRequest().body(
                ErrorResponse.createNoAuthorSpecified()
            );
        }
    }

    @GetMapping(value = "/books/{isbn}")
    public ResponseEntity<?> getBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn
    ) {
        try {
            CatalogBook book = this.catalogService.getBook(ISBNCleaner.clean(isbn));
            return ResponseEntity.ok(FullCatalogBookResultMapper.MAPPER.fromCatalogBook(book));
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        }
    }

    @DeleteMapping(value = "/books/{isbn}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> removeBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn
    ) {
        try {
            this.catalogService.removeBook(ISBNCleaner.clean(isbn));
            return ResponseEntity.ok().build();
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createBookNotFound(e.getIsbn())
            );
        }
    }

    @PostMapping(value = "/authors")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> registerAuthor(
        @RequestBody @Valid AuthorRegistrationRequest request
    ) {
        Author author = AuthorMapper.MAPPER.fromAuthorRegistrationRequest(request);

        this.catalogService.registerAuthor(author);
        return ResponseEntity.created(URI.create("/authors/" + author.getId())).build();
    }

    @PatchMapping(value = "/authors/{authorId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> editAuthor(
        @PathVariable("authorId") @NotNull @ValidAuthorId String authorId,
        @RequestBody @Valid EditAuthorRequest request
    ) {
        PartialAuthor partialAuthor = PartialAuthorMapper.MAPPER.fromEditAuthorRequest(request);

        try {
            this.catalogService.editAuthor(Long.parseLong(authorId), partialAuthor);
            return ResponseEntity.ok().build();
        } catch (AuthorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createAuthorNotFound(Long.toString(e.getAuthorId()))
            );
        }
    }

    @DeleteMapping(value = "/authors/{authorId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> removeAuthor(
        @PathVariable("authorId") @NotNull @ValidAuthorId String authorId
    ) {
        try {
            this.catalogService.removeAuthor(Long.parseLong(authorId));
        } catch (AuthorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createAuthorNotFound(Long.toString(e.getAuthorId()))
            );
        } catch (AuthorHasBooksException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createAuthorHasBooks(Long.toString(e.getAuthorId()))
            );
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/publishers")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> registerPublisher(
        @RequestBody @Valid PublisherRegistrationRequest request
    ) {
        Publisher publisher = PublisherMapper.MAPPER.fromPublisherRegistrationRequest(request);

        this.catalogService.registerPublisher(publisher);
        return ResponseEntity.created(URI.create("/publishers/" + publisher.getId())).build();
    }

    @PatchMapping(value = "/publishers/{publisherId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> editPublisher(
        @PathVariable("publisherId") @NotNull @ValidPublisherId String publisherId,
        @RequestBody @Valid EditPublisherRequest request
    ) {
        PartialPublisher partialPublisher = PartialPublisherMapper.MAPPER.fromEditPublisherRequest(request);

        try {
            this.catalogService.editPublisher(Long.parseLong(publisherId), partialPublisher);
            return ResponseEntity.ok().build();
        } catch (PublisherNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createPublisherNotFound(Long.toString(e.getPublisherId()))
            );
        }
    }

    @DeleteMapping(value = "/publishers/{publisherId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> removePublisher(
        @PathVariable("publisherId") @NotNull @ValidPublisherId String publisherId
    ) {
        try {
            this.catalogService.removePublisher(Long.parseLong(publisherId));

            return ResponseEntity.ok().build();
        } catch (PublisherNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createPublisherNotFound(Long.toString(e.getPublisherId()))
            );
        } catch (PublisherHasBooksException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createPublisherHasBooks(Long.toString(e.getPublisherId()))
            );
        }
    }

    @GetMapping(value = "/publishers/{publisherId}")
    public ResponseEntity<?> getPublisher(
        @PathVariable("publisherId") @NotNull @ValidPublisherId String publisherId
    ) {
        try {
            FullPublisherResult result = FullPublisherResultMapper.MAPPER.fromPublishers(
                this.catalogService.getPublisher(Long.parseLong(publisherId)));

            return ResponseEntity.ok(result);
        } catch (PublisherNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.createPublisherHasBooks(Long.toString(e.getPublisherId()))
            );
        }
    }

    private static final int PUBLISHER_SEARCH_MAX_PAGE_SIZE = 5;

    @GetMapping(value = "/publishers")
    public ResponseEntity<List<PublisherSearchResultEntry>> searchPublishers(
        @RequestParam(name = "query", required = false) String query,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "pageSize", required = false) Integer pageSize)
    {
        pageSize = pageSize == null ? PUBLISHER_SEARCH_MAX_PAGE_SIZE : Math.min(PUBLISHER_SEARCH_MAX_PAGE_SIZE, pageSize);
        List<Publisher> publishers = this.catalogService.searchPublishers(query, pageSize, page);

        return ResponseEntity.ok(PublisherSearchResultEntryMapper.MAPPER.fromPublishers(publishers));
    }

    @GetMapping(value = "/authors/{authorId}")
    public ResponseEntity<?> getAuthor(
        @PathVariable("authorId") @NotNull @ValidAuthorId String authorId
    ) {
        FullAuthorResult result = null;
        try {
            result = FullAuthorResultMapper.MAPPER.fromAuthor(
                this.catalogService.getAuthor(Long.parseLong(authorId)));
        } catch (AuthorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.createAuthorNotFound(Long.toString(e.getAuthorId()))
            );
        }

        return ResponseEntity.ok(result);
    }

    private static final int AUTHORS_SEARCH_MAX_PAGE_SIZE = 5;

    @GetMapping(value = "/authors")
    public ResponseEntity<?> searchAuthors(
        @RequestParam(name = "query", required = false) String query,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "pageSize", required = false) Integer pageSize)
    {
        pageSize = pageSize == null ? AUTHORS_SEARCH_MAX_PAGE_SIZE : Math.min(AUTHORS_SEARCH_MAX_PAGE_SIZE, pageSize);
        List<Author> authors = this.catalogService.searchAuthors(query, pageSize, page);

        return ResponseEntity.ok(AuthorSearchResultEntryMapper.MAPPER.fromAuthors(authors));
    }
}
