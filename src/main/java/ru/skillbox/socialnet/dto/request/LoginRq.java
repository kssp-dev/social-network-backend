package ru.skillbox.socialnet.dto.request;

import lombok.Data;

@Data
public class LoginRq {
    String email;
    String password;
}
