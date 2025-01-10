package com.example.backend.repository;

import com.example.backend.model.User;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);


    @NotNull
    Optional<User> findById(@NotNull Integer userId);

    boolean existsById(@NotNull Integer userId);

    @Modifying
    @Transactional
    void deleteById(@NotNull Integer userId);

}