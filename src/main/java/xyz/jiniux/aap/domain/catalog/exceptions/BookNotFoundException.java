package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String isbn) {
        super("the book with ISBN " + isbn + " was not found");
        this.isbn = isbn;
    }

    private final String isbn;
}
