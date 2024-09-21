package xyz.jiniux.aap.domain.catalog.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.ISBN;
import xyz.jiniux.aap.validation.*;

import java.io.Serializable;

@Data
public class BookRegistrationRequest implements Serializable {
    @NotNull
    @ISBN(type = ISBN.Type.ISBN_13)
    private String isbn;

    @NotNull
    @ValidBookTitle
    private String title;

    @NotNull
    @ValidBookDescription
    private String description;

    @NotNull
    @ValidBookPublicationYear
    private Integer publicationYear = -1;

    @NotNull
    @ValidBookEdition
    private String edition;

    @NotNull
    private ValidSet<@ValidAuthorId String> authorIds;

    @NotNull
    @ValidPublisherId
    private String publisherId;
}
