package xyz.jiniux.aap.domain.catalog.requests;

import lombok.Getter;

@Getter
public class AuthorSearchQuery {
    public static final AuthorSearchQuery EMPTY = new AuthorSearchQuery();

    private String queryString;
    private int maxResultCount = 5;
    private int page = 0;

    public AuthorSearchQuery() {}

    public AuthorSearchQuery(String queryString, int maxResultCount, int page) {
        this.queryString = queryString;
        this.maxResultCount = maxResultCount;
        this.page = page;
    }
}
