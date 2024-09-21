package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class PublisherDoesNotExistException extends Exception {
    public PublisherDoesNotExistException(String publisherId) {
        super("publisher with id " + publisherId + " does not exist");
        this.publisherId = publisherId;
    }

    private final String publisherId;
}
