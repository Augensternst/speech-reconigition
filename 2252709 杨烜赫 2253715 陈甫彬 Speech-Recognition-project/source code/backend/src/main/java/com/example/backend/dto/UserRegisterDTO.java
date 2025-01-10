package com.example.backend.dto;

import com.example.backend.model.User;
import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String email;
}
