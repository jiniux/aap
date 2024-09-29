package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class PublisherHasBooksException extends Exception {
    public PublisherHasBooksException(long publisherId) {
        super("the publisher with id " + publisherId +  "is assigned to at least one book, it cannot be deleted");
        this.publisherId = publisherId;
    }

    private final long publisherId;
}
