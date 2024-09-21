package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class AuthorHasBooksException extends Exception {
    public AuthorHasBooksException(String authorId) {
        super("the author with id " + authorId + "is assigned to at least one book, it cannot be deleted");
        this.authorId = authorId;
    }

    private final String authorId;
}
