package ru.skillbox.socialnet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.socialnet.dto.request.CommentRq;
import ru.skillbox.socialnet.dto.response.*;
import ru.skillbox.socialnet.entity.personrelated.FriendShip;
import ru.skillbox.socialnet.entity.enums.FriendShipStatus;
import ru.skillbox.socialnet.entity.enums.LikeType;
import ru.skillbox.socialnet.entity.locationrelated.Weather;
import ru.skillbox.socialnet.entity.postrelated.Post;
import ru.skillbox.socialnet.entity.postrelated.PostComment;
import ru.skillbox.socialnet.exception.post.PostCommentCreateException;
import ru.skillbox.socialnet.exception.post.PostCommentNotFoundException;
import ru.skillbox.socialnet.exception.post.PostNotFoundException;
import ru.skillbox.socialnet.mapper.CommentMapper;
import ru.skillbox.socialnet.mapper.WeatherMapper;
import ru.skillbox.socialnet.repository.*;
import ru.skillbox.socialnet.security.JwtTokenUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostCommentsService {
    private final PostCommentsRepository postCommentsRepository;
    private final PostsRepository postsRepository;
    private final LikesRepository likesRepository;
    private final FriendShipRepository friendShipRepository;
    private final WeatherRepository weatherRepository;

    private final JwtTokenUtils jwtTokenUtils;
    private final CommentMapper commentMapper;
    private final WeatherMapper weatherMapper;

    @Transactional
    public CommonRs<CommentRs> createComment(String authorization, Long postId, CommentRq commentRq) {
        Long myId = jwtTokenUtils.getId(authorization);

        if (commentRq.getCommentText() == null || commentRq.getCommentText().isBlank()) {
            throw new PostCommentCreateException("Comment text is absent");
        }

        Post post = fetchPost(postId, false);

        PostComment postComment = new PostComment();

        postComment.setPost(post);
        postComment.setAuthor(post.getAuthor());
        postComment.setTime(LocalDateTime.now());
        postComment.setIsBlocked(false);
        postComment.setIsDeleted(false);

        return updatePostComment(postComment, commentRq, myId);
    }

    @Transactional
    public CommonRs<CommentRs> editComment(String authorization, Long id, Long commentId, CommentRq commentRq) {
        Long myId = jwtTokenUtils.getId(authorization);

        return updatePostComment(
                fetchPostComment(
                        commentId, id,
                        commentRq.isDeleted == null ? false : !commentRq.isDeleted
                ),
                commentRq, myId
        );
    }

    @Transactional
    public CommonRs<CommentRs> deleteComment(String authorization, Long id, Long commentId) {
        CommentRq commentRq = new CommentRq();
        commentRq.setIsDeleted(true);

        return editComment(authorization, id, commentId, commentRq);
    }

    @Transactional
    public CommonRs<CommentRs> recoverComment(String authorization, Long id, Long commentId) {
        CommentRq commentRq = new CommentRq();
        commentRq.setIsDeleted(false);

        return editComment(authorization, id, commentId, commentRq);
    }

    @Transactional
    public CommonRs<List<CommentRs>> getComments(String authorization, Long postId, Integer offset, Integer perPage) {
        Long myId = jwtTokenUtils.getId(authorization);
        Post post = fetchPost(postId, false);

        List<PostComment> postComments = postCommentsRepository.findAllByPostIdAndIsDeleted(
                post.getId(),
                false,
                PageRequest.of(
                        offset, perPage,
                        Sort.by("time").descending()
                )
        );

        long total = postCommentsRepository.countByPostIdAndIsDeleted(
                post.getId(), false
        );

        return getListPostCommentResponse(postComments, total, myId, offset, perPage);
    }


    protected PostComment fetchPostComment(Long id, Long postId, Boolean isDeleted) {
        Optional<PostComment> optionalPostComment = postCommentsRepository.findByIdAndPostIdAndIsDeleted(
                id, postId, isDeleted
        );

        if (optionalPostComment.isEmpty()) {
            throw new PostCommentNotFoundException(id);
        }

        return optionalPostComment.get();
    }

    private Post fetchPost(Long id, Boolean isDeleted) {
        Optional<Post> optionalPost;

        if (isDeleted == null) {
            optionalPost = postsRepository.findById(id);
        } else {
            optionalPost = postsRepository.findByIdAndIsDeleted(id, isDeleted);
        }

        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException(id);
        }

        return optionalPost.get();
    }

    private CommonRs<CommentRs> updatePostComment(PostComment postComment, CommentRq commentRq, Long myId) {
        savePostComment(postComment, commentRq);

        return getPostCommentResponse(postComment, myId);
    }

    private void savePostComment(PostComment postComment, CommentRq commentRq) {
        if (commentRq.getParentId() != null) {
            PostComment parentPostComment = fetchPostComment(
                    commentRq.getParentId(), postComment.getPost().getId(), false
            );

            if (parentPostComment.getParentId() != null) {
                throw new PostCommentCreateException("Subcomment of subcomment is not allowed");
            }
        }

        postComment.setParentId(null);

        commentMapper.commentRqToPostComment(commentRq, postComment);
        postCommentsRepository.save(postComment);
    }

    private CommonRs<List<CommentRs>> getListPostCommentResponse(
            List<PostComment> postComments, Long total, Long myId, Integer offset, Integer perPage
    ) {
        CommonRs<List<CommentRs>> commonRsListCommentRs = new CommonRs<>();
        commonRsListCommentRs.setOffset(offset);
        commonRsListCommentRs.setItemPerPage(perPage);
        commonRsListCommentRs.setPerPage(perPage);
        commonRsListCommentRs.setTotal(total);

        List<CommentRs> commentRsList = new ArrayList<>();

        for (PostComment postComment : postComments) {
            commentRsList.add(postCommentToCommentRs(postComment, myId));
        }

        commonRsListCommentRs.setData(commentRsList);

        return commonRsListCommentRs;
    }

    private CommonRs<CommentRs> getPostCommentResponse(PostComment postComment, Long myId) {
        CommonRs<CommentRs> commonRsCommentRs = new CommonRs<>();

        commonRsCommentRs.setData(postCommentToCommentRs(postComment, myId));

        return commonRsCommentRs;
    }

    private CommentRs postCommentToCommentRs(PostComment postComment, Long myId) {
        CommentRs commentRs = commentMapper.postCommentToCommentRs(postComment);

        commentRs.setLikes(likesRepository.countByTypeAndEntityId(LikeType.Comment, commentRs.getId()));
        commentRs.setMyLike(likesRepository.existsByPersonId(myId));

        fillAuthor(commentRs.getAuthor(), myId);

        return commentRs;
    }

    private FriendShipStatus getFriendshipStatus(Long personId, Long destinationPersonId) {
        Optional<FriendShip> optionalFriendShip = friendShipRepository
                    .findBySrcPersonIdAndDstPersonId(personId, destinationPersonId);

        if (optionalFriendShip.isEmpty()) {
            return FriendShipStatus.UNKNOWN;
        }

        return optionalFriendShip.get().getStatus();
    }

    private PersonRs fillAuthor(PersonRs personRs, Long myId) {
        FriendShipStatus friendshipStatus = getFriendshipStatus(personRs.getId(), myId);
        personRs.setFriendStatus(friendshipStatus.toString());
        personRs.setIsBlockedByCurrentUser(friendshipStatus == FriendShipStatus.BLOCKED);

        Optional<Weather> optionalWeather = weatherRepository.findByCity(personRs.getCity());

        if (optionalWeather.isPresent()) {
            personRs.setWeather(weatherMapper.weatherToWeatherRs(optionalWeather.get()));
        }

        return personRs;
    }
}
