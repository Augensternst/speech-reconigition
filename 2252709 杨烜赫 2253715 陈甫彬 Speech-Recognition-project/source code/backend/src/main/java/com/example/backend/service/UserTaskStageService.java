package com.example.backend.service;

import com.example.backend.model.Task;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserTaskStageService {
    Integer getStage(Integer userId, Integer taskId);
    List<Task> getTasks(Integer userId);

    @Transactional
    void updateStage(Integer userId, Integer taskId, Integer stage);
}
