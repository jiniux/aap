package xyz.jiniux.aap.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;
import xyz.jiniux.aap.domain.model.BookCategory;

@Mapper
public interface BookCategoryMapper {
    BookCategoryMapper MAPPER = Mappers.getMapper(BookCategoryMapper.class);

    @ValueMapping(target = "FICTION", source = "fiction")
    @ValueMapping(target = "NON_FICTION", source = "non-fiction")
    @ValueMapping(target = "SCIENCE_FICTION", source = "science-fiction")
    @ValueMapping(target = "MYSTERY", source = "mystery")
    @ValueMapping(target = "FANTASY", source = "fantasy")
    @ValueMapping(target = "BIOGRAPHY", source = "biography")
    @ValueMapping(target = "HISTORY", source = "history")
    @ValueMapping(target = "SELF_HELP", source = "self-help")
    @ValueMapping(target = "THRILLER", source = "thriller")
    @ValueMapping(target = "ROMANCE", source = "romance")
    @ValueMapping(target = "CHILDREN", source = "children")
    @ValueMapping(target = "POETRY", source = "poetry")
    @ValueMapping(target = "HORROR", source = "horror")
    @ValueMapping(target = "TECHNOLOGY", source = "technology")
    @ValueMapping(target = "BUSINESS", source = "business")
    BookCategory fromString(String value);

    @ValueMapping(target = "fiction", source = "FICTION")
    @ValueMapping(target = "non-fiction", source = "NON_FICTION")
    @ValueMapping(target = "science-fiction", source = "SCIENCE_FICTION")
    @ValueMapping(target = "mystery", source = "MYSTERY")
    @ValueMapping(target = "fantasy", source = "FANTASY")
    @ValueMapping(target = "biography", source = "BIOGRAPHY")
    @ValueMapping(target = "history", source = "HISTORY")
    @ValueMapping(target = "self-help", source = "SELF_HELP")
    @ValueMapping(target = "thriller", source = "THRILLER")
    @ValueMapping(target = "romance", source = "ROMANCE")
    @ValueMapping(target = "children", source = "CHILDREN")
    @ValueMapping(target = "poetry", source = "POETRY")
    @ValueMapping(target = "horror", source = "HORROR")
    @ValueMapping(target = "technology", source = "TECHNOLOGY")
    @ValueMapping(target = "business", source = "BUSINESS")
    String toString(BookCategory value);
}
