package com.example.backend.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private Integer UserId;
    private String username;
    private String password;
    private String email;
}
