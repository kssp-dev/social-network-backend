package ru.skillbox.socialnet.dto.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUsersSearchPs {
    private int ageFrom;
    private int ageTo;
    private String city;
    private String country;
    private String firstName;
    private String lastName;
}
