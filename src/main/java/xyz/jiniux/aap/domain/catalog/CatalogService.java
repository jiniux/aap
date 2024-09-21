package xyz.jiniux.aap.domain.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.catalog.exceptions.*;
import xyz.jiniux.aap.domain.catalog.requests.*;
import xyz.jiniux.aap.domain.catalog.results.AuthorRegistrationResult;
import xyz.jiniux.aap.domain.catalog.results.BookSearchResult;
import xyz.jiniux.aap.domain.catalog.results.FullCatalogBookResult;
import xyz.jiniux.aap.domain.catalog.results.PublisherRegistrationResult;
import xyz.jiniux.aap.infrastructure.persistency.AuthorRepository;
import xyz.jiniux.aap.infrastructure.persistency.CatalogBookRepository;
import xyz.jiniux.aap.infrastructure.persistency.PublisherRepository;
import xyz.jiniux.aap.mappers.*;
import xyz.jiniux.aap.model.Author;
import xyz.jiniux.aap.model.CatalogBook;
import xyz.jiniux.aap.model.Publisher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CatalogService {
    private final CatalogBookRepository catalogBookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final EntityManager entityManager;

    public CatalogService(
        CatalogBookRepository catalogBookRepository,
        AuthorRepository authorRepository,
        PublisherRepository publisherRepository,
        EntityManager entityManager)
    {
        this.catalogBookRepository = catalogBookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void registerBook(@NonNull BookRegistrationRequest request)
        throws AuthorsDoNotExistException,
        PublisherDoesNotExistException,
        ISBNAlreadyRegisteredException,
        NoAuthorSpecifiedException
    {
        CatalogBook book = CatalogBookMapper.MAPPER.fromBookRegistrationRequest(request);

        if (catalogBookRepository.existsByIsbn(book.getIsbn()))
            throw new ISBNAlreadyRegisteredException(book.getIsbn());

        if (book.getAuthorIds().isEmpty())
            throw new NoAuthorSpecifiedException();

        if (authorRepository.findAllIds(book.getAuthorIds()).size() != book.getAuthorIds().size())
            throw new AuthorsDoNotExistException(authorRepository.getMissingAuthorIds(book.getAuthorIds()));

        if (publisherRepository.findByIdOptimistic(book.getPublisherId()) == null)
            throw new PublisherDoesNotExistException(book.getPublisherId().toString());

        catalogBookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public BookSearchResult searchBooks(@NonNull BookSearchQuery query) {
        Pageable pageable = Pageable.ofSize(query.getMaxResultCount()).withPage(query.getPage());

        List<CatalogBook> books;
        if (query.getQueryString() != null) {
            books = catalogBookRepository.searchCatalogBooks(query.getQueryString(), pageable);
        } else {
            books = catalogBookRepository.searchCatalogBooks(pageable);
        }

        return new BookSearchResult(BookSearchResultEntryMapper.MAPPER.fromCatalogBooks(books));
    }

    @Transactional(readOnly = true)
    public FullCatalogBookResult getBook(@NonNull String isbn) throws BookNotFoundException {
        return catalogBookRepository.findCatalogBookByIsbn(isbn)
            .map(FullCatalogBookResultMapper.MAPPER::fromCatalogBook)
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @Transactional
    public void removeBook(@NonNull String isbn) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        catalogBookRepository.delete(catalogBook);
    }

    @Transactional
    public void editBook(@NonNull String isbn, @NonNull EditBookRequest request)
        throws BookNotFoundException,
        AuthorsDoNotExistException,
        PublisherDoesNotExistException,
        NoAuthorSpecifiedException
    {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        entityManager.lock(catalogBook, LockModeType.OPTIMISTIC);

        if (request.getEdition() != null)
            catalogBook.setEdition(request.getEdition());

        if (request.getPublicationYear() != null)
            catalogBook.setPublicationYear(request.getPublicationYear());

        if (request.getTitle() != null)
            catalogBook.setTitle(request.getTitle());

        if (request.getDescription() != null)
            catalogBook.setDescription(request.getDescription());

        if (request.getAuthorIds() != null) {
            if (request.getAuthorIds().isEmpty())
                throw new NoAuthorSpecifiedException();

            Set<Long> authorIds = request.getAuthorIds()
                .stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());

            if (!authorRepository.doAllAuthorsExist(authorIds))
                throw new AuthorsDoNotExistException(authorRepository.getMissingAuthorIds(authorIds));

            catalogBook.setAuthorIds(authorIds);
        }

        if (request.getPublisherId() != null) {
            long publisherId = Long.parseLong(request.getPublisherId());

            Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherDoesNotExistException(request.getPublisherId()));

            catalogBook.setPublisher(publisher);
        }
    }

    @Transactional
    public AuthorRegistrationResult registerAuthor(@NonNull AuthorRegistrationRequest request) {
        Author author = AuthorMapper.MAPPER.fromAuthorRegistrationRequest(request);
        authorRepository.save(author);

        return new AuthorRegistrationResult(author.getId().toString());
    }

    @Transactional
    public void editAuthor(@NonNull String authorId, @NonNull EditAuthorRequest request)
        throws AuthorNotFoundException
    {
        Author author = authorRepository.findById(Long.parseLong(authorId))
            .orElseThrow(() -> new AuthorNotFoundException(authorId));

        entityManager.lock(author, LockModeType.OPTIMISTIC);

        if (request.firstName() != null)
            author.setFirstName(request.firstName());

        if (request.lastName() != null)
            author.setLastName(request.lastName());

        authorRepository.save(author);
    }

    @Transactional
    public void removeAuthor(@NonNull String authorId, @NonNull EditAuthorRequest request)
        throws AuthorNotFoundException,
        AuthorHasBooksException
    {
        Author author = authorRepository.findById(Long.parseLong(authorId))
            .orElseThrow(() -> new AuthorNotFoundException(authorId));

        boolean authorHasBooks = catalogBookRepository.countCatalogBookByAuthorId(author.getId()) > 0;

        if (authorHasBooks)
            throw new AuthorHasBooksException(authorId);

        authorRepository.delete(author);
    }

    @Transactional
    public PublisherRegistrationResult registerPublisher(@NonNull PublisherRegistrationRequest request) {
        Publisher publisher = PublisherMapper.MAPPER.fromPublisherRegistrationRequest(request);
        publisherRepository.save(publisher);

        return new PublisherRegistrationResult(publisher.getId().toString());
    }

    @Transactional
    public void editPublisher(@NonNull String publisherId, @NonNull EditPublisherRequest request)
        throws PublisherNotFoundException
    {
        Publisher publisher = publisherRepository.findById(Long.parseLong(publisherId))
            .orElseThrow(() -> new PublisherNotFoundException(publisherId));

        entityManager.lock(publisher, LockModeType.OPTIMISTIC);

        if (request.name() != null)
            publisher.setName(request.name());

        publisherRepository.save(publisher);
    }

    @Transactional
    public void removePublisher(@NonNull String publisherId, @NonNull EditPublisherRequest request)
        throws PublisherNotFoundException,
        PublisherHasBooksException
    {
        Publisher publisher = publisherRepository.findById(Long.parseLong(publisherId))
            .orElseThrow(() -> new PublisherNotFoundException(publisherId));

        boolean publisherHasBooks = catalogBookRepository.countCatalogBookByPublisherId(publisher.getId()) > 0;

        if (publisherHasBooks)
            throw new PublisherHasBooksException(publisherId);

        publisherRepository.delete(publisher);
    }
}
