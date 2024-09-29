package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.controllers.requests.AuthorRegistrationRequest;
import xyz.jiniux.aap.domain.model.Author;

@Mapper
public interface AuthorMapper {
    AuthorMapper MAPPER = Mappers.getMapper(AuthorMapper.class);

    Author fromAuthorRegistrationRequest(AuthorRegistrationRequest request);
}
