package com.example.backend.model;

import com.example.backend.dto.UserRegisterDTO;
import com.example.backend.dto.UserUpdateDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "registration_date")
    private Instant registrationDate;

    private String avatar;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserTaskStage> userTaskStages = new LinkedHashSet<>();

    public User(UserRegisterDTO user) {
        this.username = user.getUsername();
        this.passwordHash = user.getPassword();
        this.email = user.getEmail();

    }


    public User(UserUpdateDTO user) {
        this.id = user.getUserId();
        this.username = user.getUsername();
        this.passwordHash = user.getPassword();
        this.email = user.getEmail();
    }


    public User(String username, String encodePassword, Instant instant) {
        this.username = username;
        this.passwordHash = encodePassword;
        this.registrationDate = instant;
    }

    public User(String username, String encodePassword, String email, String avatar) {
        this.username = username;
        this.passwordHash = encodePassword;
        this.email = email;
        this.avatar = avatar;
    }
}