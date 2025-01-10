package com.example.backend.repository;

import com.example.backend.model.Image;
import com.example.backend.model.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    @NotNull
    Optional<Image> findById(@NotNull Integer imageId);
    Set<Image> findByTaskID(@NotNull Task taskId);
    void deleteById(@NotNull Integer imageId);
}
