package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class PublisherNotFoundException extends Exception {
    public PublisherNotFoundException(String publisherId) {
        super("the publisher with id " + publisherId + " was not found");
        this.publisherId = publisherId;
    }

    private final String publisherId;
}
