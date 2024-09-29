package xyz.jiniux.aap.domain.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import xyz.jiniux.aap.domain.catalog.exceptions.*;
import xyz.jiniux.aap.infrastructure.persistency.AuthorRepository;
import xyz.jiniux.aap.infrastructure.persistency.CatalogBookRepository;
import xyz.jiniux.aap.infrastructure.persistency.PublisherRepository;
import xyz.jiniux.aap.domain.model.Author;
import xyz.jiniux.aap.domain.model.CatalogBook;
import xyz.jiniux.aap.domain.model.Publisher;

import java.util.List;
import java.util.Set;

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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Retryable(retryFor = CannotAcquireLockException.class)
    public void registerBook(@NonNull CatalogBook book)
        throws AuthorsNotFoundException,
        PublisherNotFoundException,
        ISBNAlreadyRegisteredException,
        NoAuthorSpecifiedException
    {
        if (book.getId() != null)
            throw new IllegalArgumentException("catalogBook has already an id");

        if (catalogBookRepository.existsByIsbn(book.getIsbn()))
            throw new ISBNAlreadyRegisteredException(book.getIsbn());

        if (book.getAuthorIds().isEmpty())
            throw new NoAuthorSpecifiedException();

        if (!authorRepository.doAllAuthorsExist(book.getAuthorIds()))
            throw new AuthorsNotFoundException(authorRepository.getMissingAuthorIds(book.getAuthorIds()));

        if (!publisherRepository.existsById(book.getPublisherId()))
            throw new PublisherNotFoundException(book.getPublisherId());

        catalogBookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<CatalogBook> searchBooks(String query, int maxResultCount, int page) {
        Pageable pageable = Pageable.ofSize(maxResultCount).withPage(page);

        List<CatalogBook> books;
        if (query != null) {
            books = catalogBookRepository.searchCatalogBooks(query, pageable);
        } else {
            books = catalogBookRepository.searchCatalogBooks(pageable);
        }

        return books;
    }

    @Transactional(readOnly = true)
    public CatalogBook getBook(@NonNull String isbn) throws BookNotFoundException {
        return catalogBookRepository.findCatalogBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @Transactional
    public void removeBook(@NonNull String isbn) throws BookNotFoundException {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        entityManager.lock(catalogBook, LockModeType.OPTIMISTIC);

        catalogBookRepository.delete(catalogBook);
    }

    @Transactional
    public void editBook(@NonNull String isbn, @NonNull PartialCatalogBook partialCatalogBook)
        throws BookNotFoundException,
        AuthorsNotFoundException,
        PublisherNotFoundException,
        NoAuthorSpecifiedException
    {
        CatalogBook catalogBook = catalogBookRepository.findCatalogBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        entityManager.lock(catalogBook, LockModeType.OPTIMISTIC);

        if (partialCatalogBook.getEdition() != null)
            catalogBook.setEdition(partialCatalogBook.getEdition());

        if (partialCatalogBook.getPublicationYear() != null)
            catalogBook.setPublicationYear(partialCatalogBook.getPublicationYear());

        if (partialCatalogBook.getTitle() != null)
            catalogBook.setTitle(partialCatalogBook.getTitle());

        if (partialCatalogBook.getDescription() != null)
            catalogBook.setDescription(partialCatalogBook.getDescription());

        if (partialCatalogBook.getAuthorIds() != null) {
            Set<Long> authorIds = partialCatalogBook.getAuthorIds();

            if (authorIds.isEmpty())
                throw new NoAuthorSpecifiedException();

            if (!authorRepository.doAllAuthorsExist(authorIds))
                throw new AuthorsNotFoundException(authorRepository.getMissingAuthorIds(authorIds));

            catalogBook.setAuthorIds(authorIds);
        }

        if (partialCatalogBook.getPublisherId() != null) {
            long publisherId = partialCatalogBook.getPublisherId();

            Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherNotFoundException(publisherId));

            catalogBook.setPublisher(publisher);
        }

        catalogBookRepository.save(catalogBook);
    }

    @Transactional
    public void registerAuthor(@NonNull Author author) {
        if (author.getId() != null)
            throw new IllegalArgumentException("author has already an id");

        authorRepository.save(author);
    }

    @Transactional
    public void editAuthor(@NonNull long authorId, @NonNull PartialAuthor partialAuthor)
        throws AuthorNotFoundException
    {
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException(authorId));

        entityManager.lock(author, LockModeType.OPTIMISTIC);

        if (partialAuthor.firstName() != null)
            author.setFirstName(partialAuthor.firstName());

        if (partialAuthor.lastName() != null)
            author.setLastName(partialAuthor.lastName());

        authorRepository.save(author);
    }

    @Transactional
    public void removeAuthor(@NonNull long authorId)
        throws AuthorNotFoundException,
        AuthorHasBooksException
    {
        Author author = authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException(authorId));

        entityManager.lock(author, LockModeType.OPTIMISTIC);

        boolean authorHasBooks = catalogBookRepository.countCatalogBookByAuthorId(author.getId()) > 0;

        if (authorHasBooks)
            throw new AuthorHasBooksException(authorId);

        authorRepository.delete(author);
    }

    @Transactional
    public void registerPublisher(@NonNull Publisher publisher) {
        if (publisher.getId() != null)
            throw new IllegalArgumentException("publisher has already an id");

        publisherRepository.save(publisher);
    }

    @Transactional
    public void editPublisher(long publisherId, @NonNull PartialPublisher partialPublisher)
        throws PublisherNotFoundException
    {
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new PublisherNotFoundException(publisherId));

        entityManager.lock(publisher, LockModeType.OPTIMISTIC);

        if (partialPublisher.name() != null)
            publisher.setName(partialPublisher.name());

        publisherRepository.save(publisher);
    }

    @Transactional
    public void removePublisher(long publisherId)
        throws PublisherNotFoundException,
        PublisherHasBooksException
    {
        Publisher publisher = publisherRepository.findById(publisherId)
            .orElseThrow(() -> new PublisherNotFoundException(publisherId));

        entityManager.lock(publisher, LockModeType.OPTIMISTIC);

        boolean publisherHasBooks = catalogBookRepository.countCatalogBookByPublisherId(publisher.getId()) > 0;

        if (publisherHasBooks)
            throw new PublisherHasBooksException(publisherId);

        publisherRepository.delete(publisher);
    }

    @Transactional(readOnly = true)
    public Publisher getPublisher(@NonNull long publisherId)
        throws PublisherNotFoundException
    {
        return publisherRepository.findById(publisherId)
            .orElseThrow(() -> new PublisherNotFoundException(publisherId));
    }

    @Transactional(readOnly = true)
    public List<Publisher> searchPublishers(String query, int maxResultCount, int page) {
        Pageable pageable = Pageable.ofSize(maxResultCount).withPage(page);

        List<Publisher> publishers;
        if (query != null) {
            publishers = publisherRepository.searchPublishers(query, pageable);
        } else {
            publishers = publisherRepository.searchPublishers(pageable);
        }

        return publishers;
    }

    @Transactional(readOnly = true)
    public List<Author> searchAuthors(String query, int maxResultCount, int page) {
        Pageable pageable = Pageable.ofSize(maxResultCount).withPage(page);

        List<Author> authors;
        if (query != null) {
            authors = authorRepository.searchAuthors(query, pageable);
        } else {
            authors = authorRepository.searchAuthors(pageable);
        }

        return authors;
    }

    @Transactional(readOnly = true)
    public Author getAuthor(long authorId) throws AuthorNotFoundException {
        return authorRepository.findById(authorId)
            .orElseThrow(() -> new AuthorNotFoundException(authorId));
    }
}
