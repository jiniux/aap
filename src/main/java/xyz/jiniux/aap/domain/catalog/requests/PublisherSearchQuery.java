package xyz.jiniux.aap.domain.catalog.requests;

import lombok.Getter;

@Getter
public class PublisherSearchQuery {
    public static final PublisherSearchQuery EMPTY = new PublisherSearchQuery();

    private String queryString;
    private int maxResultCount = 5;
    private int page = 0;

    public PublisherSearchQuery() {}

    public PublisherSearchQuery(String queryString, int maxResultCount, int page) {
        this.queryString = queryString;
        this.maxResultCount = maxResultCount;
        this.page = page;
    }
}
