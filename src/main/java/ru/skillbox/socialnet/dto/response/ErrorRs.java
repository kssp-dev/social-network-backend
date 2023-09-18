package ru.skillbox.socialnet.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErrorRs {
    public ErrorRs(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
        timestamp = new Date().getTime();
    }

    public ErrorRs(String error) {
        this.error = error;
        this.errorDescription = error;
        timestamp = new Date().getTime();
    }

    private String error;
    private Long timestamp;
    private String errorDescription;
}
