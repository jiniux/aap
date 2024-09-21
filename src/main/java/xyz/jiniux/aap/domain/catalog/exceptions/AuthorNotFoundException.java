package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class AuthorNotFoundException extends Exception {
    public AuthorNotFoundException(String authorId) {
        super("the author with id " + authorId + " was not found");
        this.authorId = authorId;
    }

    private final String authorId;
}
