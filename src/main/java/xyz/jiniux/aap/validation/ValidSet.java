package xyz.jiniux.aap.validation;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.experimental.Delegate;

import java.util.HashSet;
import java.util.Set;

@Data
public class ValidSet<E> implements Set<E> {
    @Valid
    @Delegate
    private Set<E> set;

    public ValidSet() {
        set = new HashSet<>();
    }
}