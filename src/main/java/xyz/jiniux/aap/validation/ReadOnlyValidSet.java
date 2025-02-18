package xyz.jiniux.aap.validation;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Data
public class ReadOnlyValidSet<E> implements Set<E> {
    @Valid
    @Delegate
    private Set<E> list;

    public ReadOnlyValidSet() {
        list = Collections.emptySet();
    }

    public ReadOnlyValidSet(Collection<E> collection) {
        this.list = Set.copyOf(collection);
    }
}