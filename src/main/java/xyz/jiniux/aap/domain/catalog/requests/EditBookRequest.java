package xyz.jiniux.aap.domain.catalog.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import xyz.jiniux.aap.validation.*;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@Setter
public class EditBookRequest implements Serializable {
    @ValidBookTitle
    private String title;

    @ValidBookDescription
    private String description;

    @ValidBookPublicationYear
    private Integer publicationYear;

    @ValidBookEdition
    private String edition;

    private ValidSet<@ValidAuthorId String> authorIds;

    @ValidPublisherId
    private String publisherId;
}
