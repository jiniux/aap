package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class ISBNAlreadyRegisteredException extends Exception {
    public ISBNAlreadyRegisteredException(String isbn) {
        super("the book with ISBN " + isbn + " is already registered");
        this.isbn = isbn;
    }

    private final String isbn;
}
