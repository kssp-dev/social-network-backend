package ru.skillbox.socialnet.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import ru.skillbox.socialnet.entity.enums.MessagePermission;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PersonRs {
    private String about;
    private String city;
    private String country;
//    currency
    private String email;
    private Long id;
    private Boolean online;
    private String phone;
    private String photo;
    private String token;
//    weather
private String birth_date;
    private String first_name;
    private String friend_status;

    private Boolean is_blocked;
    private Boolean is_blocked_by_current_user;
    private String last_name;
    private String last_online_time;
    private MessagePermission messages_permission;



    private String reg_date;

    private Boolean is_deleted;
}
