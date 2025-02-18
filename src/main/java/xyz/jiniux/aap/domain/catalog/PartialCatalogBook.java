package xyz.jiniux.aap.domain.catalog;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class PartialCatalogBook {
    private String title;

    private String description;

    private Integer publicationYear;

    private String edition;

    private Set<Long> authorIds;

    private Long publisherId;
}
