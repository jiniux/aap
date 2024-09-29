package xyz.jiniux.aap.mappers;

import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.EditAuthorRequest;
import xyz.jiniux.aap.domain.catalog.PartialAuthor;

public interface PartialAuthorMapper {
    PartialAuthorMapper MAPPER = Mappers.getMapper(PartialAuthorMapper.class);

    PartialAuthor fromEditAuthorRequest(EditAuthorRequest editAuthorRequest);
}
