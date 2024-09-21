package xyz.jiniux.aap;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.jiniux.aap.domain.catalog.exceptions.*;

import java.io.Serializable;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ErrorResponse(String code, Object details) implements Serializable {}

    @ExceptionHandler(value = {AuthorsDoNotExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse authorsDoNotExistException(AuthorsDoNotExistException ex) {
        return new ErrorResponse("AUTHORS_DO_NOT_EXIST", Map.of("authorIds", ex.getIds()));
    }

    @ExceptionHandler(value = {ISBNAlreadyRegisteredException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse isbnAlreadyRegistered(ISBNAlreadyRegisteredException ex) {
        return new ErrorResponse("ISBN_ALREADY_REGISTERED", Map.of("isbn", ex.getIsbn()));
    }

    @ExceptionHandler(value = {PublisherDoesNotExistException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse publisherDoesNotExistException(PublisherDoesNotExistException ex) {
        return new ErrorResponse("PUBLISHER_DOES_NOT_EXIST", Map.of("publisherId", ex.getPublisherId()));
    }

    @ExceptionHandler(value = {BookNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse bookNotFoundException(BookNotFoundException ex) {
        return new ErrorResponse("BOOK_NOT_FOUND", Map.of("isbn", ex.getIsbn()));
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse dataIntegrityViolationException(DataIntegrityViolationException ignoredEx) {
        return new ErrorResponse("DATA_INTEGRITY_VIOLATION", Map.of());
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse optimisticLockException(DataIntegrityViolationException ignoredEx) {
        return new ErrorResponse("CONCURRENT_MODIFICATION_OCCURRED", Map.of());
    }

    // Additional exception handlers
    @ExceptionHandler(value = {PublisherNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse publisherNotFoundException(PublisherNotFoundException ex) {
        return new ErrorResponse("PUBLISHER_NOT_FOUND", Map.of("publisherId", ex.getPublisherId()));
    }

    @ExceptionHandler(value = {PublisherHasBooksException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse publisherHasBooksException(PublisherHasBooksException ex) {
        return new ErrorResponse("PUBLISHER_HAS_BOOKS", Map.of("publisherId", ex.getPublisherId()));
    }

    @ExceptionHandler(value = {AuthorNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse authorNotFoundException(AuthorNotFoundException ex) {
        return new ErrorResponse("AUTHOR_NOT_FOUND", Map.of("authorId", ex.getAuthorId()));
    }

    @ExceptionHandler(value = {AuthorHasBooksException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse authorHasBooksException(AuthorHasBooksException ex) {
        return new ErrorResponse("AUTHOR_HAS_BOOKS", Map.of("authorId", ex.getAuthorId()));
    }

    @ExceptionHandler(value = {NoAuthorSpecifiedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse authorHasBooksException(NoAuthorSpecifiedException ignoredEx) {
        return new ErrorResponse("NO_AUTHOR_SPECIFIED", Map.of());
    }
}
