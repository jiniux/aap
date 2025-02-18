package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

@Getter
public class MalformedImageException extends Exception {
    public MalformedImageException() {
        super("Image is malformed");
    }
}
