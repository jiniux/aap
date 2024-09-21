package xyz.jiniux.aap.domain.catalog.requests;

import lombok.Getter;

@Getter
public class BookSearchQuery {
    public static final BookSearchQuery EMPTY = new BookSearchQuery();

    private String queryString;
    private int maxResultCount = 20;
    private int page = 0;

    public BookSearchQuery() {}

    public BookSearchQuery(String queryString, int maxResultCount, int page) {
        this.queryString = queryString;
        this.maxResultCount = maxResultCount;
        this.page = page;
    }
}
