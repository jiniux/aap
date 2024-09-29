package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;

public record SyncShoppingCartResultRemovedItem(
    String isbn,
    String stockFormat,
    String stockQuality
) implements Serializable {}
