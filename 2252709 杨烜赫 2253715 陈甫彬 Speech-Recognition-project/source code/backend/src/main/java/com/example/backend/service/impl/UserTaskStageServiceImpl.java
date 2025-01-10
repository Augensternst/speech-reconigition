package com.example.backend.service.impl;

import com.example.backend.model.Task;
import com.example.backend.model.UserTaskStage;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserTaskStageRepository;
import com.example.backend.service.UserTaskStageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserTaskStageServiceImpl implements UserTaskStageService {
    private final UserTaskStageRepository userTaskStageRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserTaskStageServiceImpl(UserTaskStageRepository userTaskStageRepository,
                                    UserRepository userRepository,
                                    TaskRepository taskRepository) {
        this.userTaskStageRepository = userTaskStageRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Integer getStage(Integer userId, Integer taskId) {
        UserTaskStage result = userTaskStageRepository.findByIdUserIdAndIdTaskId(userId, taskId);
        return result == null ? 0 : result.getStage();
    }

    @Override
    public List<Task> getTasks(Integer userId) {
        List<UserTaskStage> result = userTaskStageRepository.findById_UserId(userId);
        List<Task> t = new ArrayList<>();
        for (UserTaskStage uts : result) {
            t.add(uts.getTask());
        }
        return t;
    }

    @Override
    @Transactional
    public void updateStage(Integer userId, Integer taskId, Integer stage) {
        // 使用正确的repository方法名
        UserTaskStage uts = userTaskStageRepository.findByIdUserIdAndIdTaskId(userId, taskId);

        if (uts == null) {
            // 使用新的构造函数创建对象，直接设置复合主键
            uts = new UserTaskStage(userId, taskId);
        }

        uts.setStage(stage);
        // 设置更新时间
        uts.setUpdateTime(Instant.now());

        // 设置关联实体
        uts.setUser(userRepository.getReferenceById(userId));
        uts.setTask(taskRepository.getReferenceById(taskId));

        userTaskStageRepository.save(uts);
    }

}
