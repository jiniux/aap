package xyz.jiniux.aap.controllers.results;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record BookSearchResult(
    List<BookSearchResultEntry> entries,
    int totalPages,
    long currentPage
) implements Serializable {

}
