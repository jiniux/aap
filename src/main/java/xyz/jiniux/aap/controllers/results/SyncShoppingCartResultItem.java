package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;

public record SyncShoppingCartResultItem(
    String isbn,
    String stockFormat,
    String stockQuality,
    int quantity
) implements Serializable {}
