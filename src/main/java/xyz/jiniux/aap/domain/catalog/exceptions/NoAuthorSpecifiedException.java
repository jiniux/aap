package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class NoAuthorSpecifiedException extends Exception {
    public NoAuthorSpecifiedException() {
        super("a book must be associated to at least one author");
    }
}
