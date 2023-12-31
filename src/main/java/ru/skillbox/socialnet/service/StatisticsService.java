package ru.skillbox.socialnet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skillbox.socialnet.dto.response.RegionStatisticsRs;
import ru.skillbox.socialnet.entity.enums.LikeType;
import ru.skillbox.socialnet.entity.personrelated.Person;
import ru.skillbox.socialnet.exception.person.PersonIsDeletedException;
import ru.skillbox.socialnet.exception.person.PersonNotFoundException;
import ru.skillbox.socialnet.repository.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final PersonRepository personRepository;
    private final TagsRepository tagsRepository;
    private final Post2tagRepository post2tagRepository;
    private final PostsRepository postsRepository;
    private final PostCommentsRepository postCommentsRepository;
    private final MessagesRepository messagesRepository;
    private final LikesRepository likesRepository;
    private final DialogsRepository dialogsRepository;
    private final CountriesRepository countriesRepository;
    private final CitiesRepository citiesRepository;

    public Long getAllUsers() {
        return personRepository.countByIsDeleted(false);
    }

    public Long getAllUsersByCountry(String country) {
        return personRepository.countByCountryAndIsDeleted(country, false);
    }

    public Long getAllUsersByCity(String city) {
        return personRepository.countByCityAndIsDeleted(city, false);
    }

    public Long getAllTags() {
        return tagsRepository.count();
    }

    public Long getTagsByPost(Long postId) {
        return post2tagRepository.countByPostId(postId);
    }

    public Long getAllPost() {
        return postsRepository.countByIsDeleted(false);
    }

    public Long getAllPostByUser(Long userId) {
        return postsRepository.countByAuthorIdAndIsDeleted(userId, false);
    }

    public Long getAllMessage() {
        return messagesRepository.countByIsDeleted(false);
    }

    public Long getMessageByDialog(Long dialogId) {
        return messagesRepository.countByDialogIdAndIsDeleted(dialogId, false);
    }

    public Map<String, Long> getMessage(Long firstUserId, Long secondUserId) {
        Map<String, Long> map = new HashMap<>();

        Person firstUser = personRepository.findById(firstUserId).orElseThrow(
                () -> new PersonNotFoundException(firstUserId)
        );
        if (firstUser.getIsDeleted()) {
            throw new PersonIsDeletedException(firstUserId);
        }

        Person secondUser = personRepository.findById(secondUserId).orElseThrow(
                () -> new PersonNotFoundException(secondUserId)
        );
        if (secondUser.getIsDeleted()) {
            throw new PersonIsDeletedException(secondUserId);
        }

        map.put(
                new StringBuilder()
                        .append(firstUser.getFirstName())
                        .append("_")
                        .append(firstUser.getLastName())
                        .append("->")
                        .append(secondUser.getFirstName())
                        .append("_")
                        .append(secondUser.getLastName())
                        .toString(),
                messagesRepository.countByAuthorIdAndRecipientId(firstUserId, secondUserId)
        );

        map.put(
                new StringBuilder()
                        .append(secondUser.getFirstName())
                        .append("_")
                        .append(secondUser.getLastName())
                        .append("->")
                        .append(firstUser.getFirstName())
                        .append("_")
                        .append(firstUser.getLastName())
                        .toString(),
                messagesRepository.countByAuthorIdAndRecipientId(secondUserId, firstUserId)
        );

        return map;
    }

    public Long getAllLike() {
        return likesRepository.count();
    }

    public Long getLikeEntity(Long entityId, LikeType type) {
        return likesRepository.countByTypeAndEntityId(type, entityId);
    }

    public Long getDialog() {
        return dialogsRepository.count();
    }

    public Long getDialogsUser(Long userId) {
        return dialogsRepository.countByFirstPersonIdOrSecondPersonId(userId, userId);
    }

    public Long getCountry() {
        return countriesRepository.count();
    }

    public List<RegionStatisticsRs> getCountryUsers() {
        List<RegionStatisticsRs> list = countriesRepository.countRegionStatistics();
        return list;
    }

    public Long getCommentsByPost(Long postId) {
        return postCommentsRepository.countByPostIdAndIsDeleted(postId, false);
    }

    public Long getAllCities() {
        return citiesRepository.count();
    }

    public List<RegionStatisticsRs> getCitiesUsers() {
        List<RegionStatisticsRs> list = citiesRepository.countRegionStatistics();
        return list;
    }
}
