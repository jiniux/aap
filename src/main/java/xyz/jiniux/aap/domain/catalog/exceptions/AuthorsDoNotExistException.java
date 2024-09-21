package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class AuthorsDoNotExistException extends Exception {
    public AuthorsDoNotExistException(Collection<?> authorIds) {
        super("authors with ids " + String.join(",", authorIds.stream().map(Object::toString).toList()) + " do not exist");
        this.ids = authorIds.stream().map(Object::toString).collect(Collectors.toSet());
    }

    private final Set<String> ids;
}
