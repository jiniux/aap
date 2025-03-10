package xyz.jiniux.aap.domain.catalog;

import lombok.Builder;
import lombok.Data;
import xyz.jiniux.aap.domain.model.BookCategory;

import java.util.List;
import java.util.Set;

@Builder
@Data
public class PartialCatalogBook {
    private String title;

    private String description;

    private Integer publicationYear;

    private String edition;

    private Set<Long> authorIds;

    private Set<BookCategory> categories;

    private Long publisherId;
}
