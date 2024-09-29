package xyz.jiniux.aap.domain.catalog.exceptions;

import lombok.Getter;

import java.util.Collection;
import java.util.Set;

@Getter
public class AuthorsNotFoundException extends Exception {
    public AuthorsNotFoundException(Collection<Long> authorIds) {
        super("authors with ids " + String.join(",", authorIds.stream().map(Object::toString).toList()) + " do not exist");
        this.ids = Set.copyOf(authorIds);
    }

    private final Set<Long> ids;
}
