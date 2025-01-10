package com.example.backend.dto;

import com.example.backend.model.User;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Instant;

@Data
public class UserAccessibleDTO {
    private Integer userId;
    private String username;
    private String email;
    private Instant registrationDate;
    public UserAccessibleDTO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.registrationDate = user.getRegistrationDate();
    }
}
