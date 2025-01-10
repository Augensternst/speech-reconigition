package com.example.backend.repository;

import com.example.backend.model.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    @NotNull
    Optional<Task> findById(@NotNull Integer taskId);
}