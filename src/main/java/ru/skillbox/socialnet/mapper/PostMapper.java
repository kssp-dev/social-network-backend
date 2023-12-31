package ru.skillbox.socialnet.mapper;

import org.mapstruct.*;

import ru.skillbox.socialnet.dto.request.PostRq;
import ru.skillbox.socialnet.dto.response.PostRs;

import ru.skillbox.socialnet.entity.postrelated.Post;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                CommentMapper.class,
                TagMapper.class,
                PersonMapper.class
        }
)
public interface PostMapper {
    PostRs postToPostRs(Post post);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Post postRqToPost(PostRq postRq, @MappingTarget Post post);
}
