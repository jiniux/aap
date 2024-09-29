package xyz.jiniux.aap.controllers.results;

import java.io.Serializable;

public record AuthorSearchResultEntry(String id, String firstName, String lastName) implements Serializable { }
