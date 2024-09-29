package xyz.jiniux.aap.domain.cart.exceptions;

public class CannotRemoveMoreThanOriginalQuantityException extends Exception {
    public CannotRemoveMoreThanOriginalQuantityException(long originalQuantity, long quantity) {
        super("tried to remove a quantity of " + quantity + " but the original quantity was " + originalQuantity);
    }
}
