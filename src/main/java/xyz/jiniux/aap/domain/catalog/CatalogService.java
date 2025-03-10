package xyz.jiniux.aap.domain.catalog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.jiniux.aap.domain.catalog.exceptions.*;
import xyz.jiniux.aap.domain.model.*;
import xyz.jiniux.aap.infrastructure.persistency.AuthorRepository;
import xyz.jiniux.aap.infrastructure.persistency.BookRepository;
import xyz.jiniux.aap.infrastructure.persistency.PublisherRepository;
import xyz.jiniux.aap.infrastructure.persistency.BookFormatPreviewImageRepository;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Service
public class CatalogService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final EntityManager entityManager;
    private final BookFormatPreviewImageRepository bookFormatPreviewImageRepository;

    public CatalogService(
        BookRepository bookRepository,
        AuthorRepository authorRepository,
        PublisherRepository publisherRepository,
        EntityManager entityManager,
        BookFormatPreviewImageRepository bookFormatPreviewImageRepository)
    {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.entityManager = entityManager;
        this.bookFormatPreviewImageRepository = bookFormatPreviewImageRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    @Retryable(retryFor = CannotAcquireLockException.class)
    public void registerBook(@NonNull Book book)
        throws AuthorsNotFoundException,
        PublisherNotFoundException,
        ISBNAlreadyRegisteredException,
        NoAuthorSpecifiedException
    {
        if (book.getId() != null)
            throw new IllegalArgumentException("book has already an id");

        if (book.getCategories().isEmpty())
            throw new IllegalArgumentException("book has no categories");

        if (bookRepository.existsByIsbn(book.getIsbn()))
            throw new ISBNAlreadyRegisteredException(book.getIsbn());

        if (book.getAuthorIds().isEmpty())
            throw new NoAuthorSpecifiedException();

        if (!authorRepository.doAllAuthorsExist(book.getAuthorIds()))
            throw new AuthorsNotFoundException(authorRepository.getMissingAuthorIds(book.getAuthorIds()));

        if (!publisherRepository.existsById(book.getPublisherId()))
            throw new PublisherNotFoundException(book.getPublisherId());

        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String query, int maxResultCount, int page) {
        Pageable pageable = Pageable.ofSize(maxResultCount).withPage(page);


        Page<Book> books;
        if (query != null) {
            books = bookRepository.searchBooks(query, pageable);
        } else {
            books = bookRepository.searchBooks(pageable);
        }

        return books;
    }

    public BookFormatPreviewImage getBookFormatPreviewImage(long bookFormatPreviewImageId)
            throws BookFormatPreviewImageNotFoundException
    {
        return bookFormatPreviewImageRepository.findById(bookFormatPreviewImageId)
            .orElseThrow(() -> new BookFormatPreviewImageNotFoundException(bookFormatPreviewImageId));
    }

    public BookFormatPreviewImage getOrCreateStockPreviewImage(@NonNull String isbn, StockFormat stockFormat)
            throws BookNotFoundException
    {
        Optional<BookFormatPreviewImage> image = bookFormatPreviewImageRepository.findByQualityAndFormat(isbn, stockFormat);

        if (image.isPresent()) {
            return image.get();
        } else {
            Book book = bookRepository.findBookByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));

            BookFormatPreviewImage stockPreviewImage = new BookFormatPreviewImage();
            stockPreviewImage.setBookId(book.getId());
            stockPreviewImage.setFormat(stockFormat);

            return stockPreviewImage;
        }
    }

    private static final int MAX_IMAGE_SIZE = 1024 * 1024;
    private static final float IMAGE_COMPRESSION_QUALITY = 0.8f;

    private byte[] processImage(MultipartFile file)
        throws IOException,
            InvalidImageFormatException,
            ImageTooBigException
    {
        if (!Objects.equals(file.getContentType(), "image/jpg") && !Objects.equals(file.getContentType(), "image/png") && !Objects.equals(file.getContentType(), "image/jpeg")) {
            String type = file.getContentType() == null ? "null" : file.getContentType();
            throw new InvalidImageFormatException(List.of("image/png", "image/jpg", "image/jpeg"), type);
        }

        if (file.getSize() == 0 && file.getSize() > MAX_IMAGE_SIZE) {
            throw new ImageTooBigException(file.getSize(), MAX_IMAGE_SIZE);
        }

        // compress image
        BufferedImage image = ImageIO.read(file.getInputStream());

        ImageWriter imageWriter = getFirstImageWriter("jpg");

        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(IMAGE_COMPRESSION_QUALITY);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageWriter.setOutput(ImageIO.createImageOutputStream(outputStream));
        imageWriter.write(null, new IIOImage(image, null, null), imageWriteParam);

        return outputStream.toByteArray();
    }

    private ImageWriter getFirstImageWriter(String format) {
        Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByFormatName(format);
        if (!imageWriterIterator.hasNext()) {
            throw new IllegalStateException("No image writer found for jpg");
        }

        return imageWriterIterator.next();
    }

    @Transactional(rollbackFor = Exception.class)
    public long uploadStockBookCover(@NonNull String isbn, StockFormat stockFormat, @NonNull MultipartFile file)
            throws BookNotFoundException, IOException, InvalidImageFormatException, ImageTooBigException, SQLException {
        BookFormatPreviewImage image = getOrCreateStockPreviewImage(isbn, stockFormat);
        image.setImage(new SerialBlob(processImage(file)));

        bookFormatPreviewImageRepository.save(image);

        return image.getId();
    }

    @Transactional(readOnly = true)
    public Book getBook(@NonNull String isbn) throws BookNotFoundException {
        return bookRepository.findBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @Transactional(rollbackFor = Exception.class)
    public void editBook(@NonNull String isbn, @NonNull PartialCatalogBook partialCatalogBook)
        throws BookNotFoundException,
        AuthorsNotFoundException,
        PublisherNotFoundException,
        NoAuthorSpecifiedException
    {
        Book book = bookRepository.findBookByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException(isbn));

        entityManager.lock(book, LockModeType.OPTIMISTIC);

        if (partialCatalogBook.getEdition() != null)
            book.setEdition(partialCatalogBook.getEdition());

        if (partialCatalogBook.getPublicationYear() != null)
            book.setPublicationYear(partialCatalogBook.getPublicationYear());

        if (partialCatalogBook.getTitle() != null)
            book.setTitle(partialCatalogBook.getTitle());

        if (partialCatalogBook.getDescription() != null)
            book.setDescription(partialCatalogBook.getDescription());

        if (partialCatalogBook.getCategories() != null)
            book.setCategories(partialCatalogBook.getCategories());

        if (partialCatalogBook.getAuthorIds() != null) {
            Set<Long> authorIds = partialCatalogBook.getAuthorIds();

            if (authorIds.isEmpty())
                throw new NoAuthorSpecifiedException();

            if (!authorRepository.doAllAuthorsExist(authorIds))
                throw new AuthorsNotFoundException(authorRepository.getMissingAuthorIds(authorIds));

            book.setAuthorIds(authorIds);
        }

        if (partialCatalogBook.getPublisherId() != null) {
            long publisherId = partialCatalogBook.getPublisherId();

            Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new PublisherNotFoundException(publisherId));

            book.setPublisher(publisher);
        }

        bookRepository.save(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registerAuthor(@NonNull Author author) {
        if (author.getId() != null)
            throw new IllegalArgumentException("author has already an id");

        authorRepository.save(author);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editAuthor(long authorId, @NonNull PartialAuthor partialAuthor)
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

    @Transactional(rollbackFor = Exception.class)
    public void registerPublisher(@NonNull Publisher publisher) {
        if (publisher.getId() != null)
            throw new IllegalArgumentException("publisher has already an id");

        publisherRepository.save(publisher);
    }

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(readOnly = true)
    public Publisher getPublisher(long publisherId)
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
