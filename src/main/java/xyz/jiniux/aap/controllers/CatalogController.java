package xyz.jiniux.aap.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.ISBN;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.jiniux.aap.domain.catalog.*;
import xyz.jiniux.aap.domain.catalog.exceptions.*;
import xyz.jiniux.aap.domain.catalog.requests.*;
import xyz.jiniux.aap.domain.catalog.results.AuthorRegistrationResult;
import xyz.jiniux.aap.domain.catalog.results.FullCatalogBookResult;
import xyz.jiniux.aap.domain.catalog.results.PublisherRegistrationResult;
import xyz.jiniux.aap.support.ISBNCleaner;
import xyz.jiniux.aap.validation.ValidAuthorId;
import xyz.jiniux.aap.validation.ValidPublisherId;

import java.net.URI;

@RestController
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PostMapping(value = "/books")
    public ResponseEntity<Void> registerBook(@RequestBody @Valid BookRegistrationRequest request)
        throws PublisherDoesNotExistException,
        AuthorsDoNotExistException,
        ISBNAlreadyRegisteredException,
        NoAuthorSpecifiedException
    {
        this.catalogService.registerBook(request);
        return ResponseEntity.created(URI.create("/books/" + ISBNCleaner.clean(request.getIsbn()))).build();
    }

    private static final int MAX_PAGE_SIZE = 50;

    @GetMapping(value = "/books")
    public ResponseEntity<?> searchBooks(
        @RequestParam(name = "query", required = false) String query,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "pageSize", required = false) Integer pageSize)
    {
        pageSize = pageSize == null ? MAX_PAGE_SIZE : Math.min(MAX_PAGE_SIZE, pageSize);
        BookSearchQuery searchQuery = new BookSearchQuery(query, pageSize, page);

        return ResponseEntity.ok(this.catalogService.searchBooks(searchQuery).entries());
    }

    // Use patch because the user does not need to specify all the fields of the book
    @PatchMapping(value = "/books/{isbn}")
    public ResponseEntity<Void> editBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn,
        @Valid @RequestBody EditBookRequest request
    ) throws PublisherDoesNotExistException,
        AuthorsDoNotExistException,
        BookNotFoundException,
        NoAuthorSpecifiedException
    {
        this.catalogService.editBook(ISBNCleaner.clean(isbn), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/books/{isbn}")
    public ResponseEntity<FullCatalogBookResult> getBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn
    ) throws BookNotFoundException
    {
        return ResponseEntity.ok(this.catalogService.getBook(ISBNCleaner.clean(isbn)));
    }

    @DeleteMapping(value = "/books/{isbn}")
    public ResponseEntity<Void> removeBook(
        @PathVariable(name = "isbn") @NotNull @ISBN String isbn
    ) throws BookNotFoundException
    {
        this.catalogService.removeBook(ISBNCleaner.clean(isbn));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/authors")
    public ResponseEntity<Void> registerAuthor(
        @RequestBody @Valid AuthorRegistrationRequest request
    ) {
        AuthorRegistrationResult result = this.catalogService.registerAuthor(request);
        return ResponseEntity.created(URI.create("/authors/" + result.authorId())).build();
    }

    @PatchMapping(value = "/authors/{authorId}")
    public ResponseEntity<Void> editAuthor(
        @PathVariable("authorId") @NotNull @ValidAuthorId String authorId,
        @RequestBody @Valid EditAuthorRequest request
    ) throws AuthorNotFoundException {
        this.catalogService.editAuthor(authorId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/authors/{authorId}")
    public ResponseEntity<Void> removeAuthor(
        @PathVariable("authorId") @NotNull @ValidAuthorId String authorId,
        @RequestBody @Valid EditAuthorRequest request
    ) throws AuthorNotFoundException, AuthorHasBooksException {
        this.catalogService.removeAuthor(authorId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/publishers")
    public ResponseEntity<Void> registerPublisher(
        @RequestBody @Valid PublisherRegistrationRequest request
    ) {
        PublisherRegistrationResult result = this.catalogService.registerPublisher(request);
        return ResponseEntity.created(URI.create("/publishers/" + result.publisherId())).build();
    }

    @PatchMapping(value = "/publishers/{publisherId}")
    public ResponseEntity<Void> editPublisher(
        @PathVariable("publisherId") @NotNull @ValidPublisherId String publisherId,
        @RequestBody @Valid EditPublisherRequest request
    ) throws PublisherNotFoundException {
        this.catalogService.editPublisher(publisherId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/publishers/{publisherId}")
    public ResponseEntity<Void> removePublisher(
        @PathVariable("publisherId") @NotNull @ValidPublisherId String publisherId,
        @RequestBody @Valid EditPublisherRequest request
    ) throws PublisherNotFoundException, PublisherHasBooksException {
        this.catalogService.removePublisher(publisherId, request);
        return ResponseEntity.ok().build();
    }


}
