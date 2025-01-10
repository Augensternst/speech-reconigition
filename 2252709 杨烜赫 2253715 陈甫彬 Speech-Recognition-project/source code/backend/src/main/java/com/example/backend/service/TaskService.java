package com.example.backend.service;

import io.swagger.v3.core.util.Json;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 任务服务类：负责任务的布置和任务管理
 */
@Service("TaskService")
public interface TaskService {
    // 创建task，返回Map格式结果
    Map<String, Object> createTask(Map<String, Object> taskPrompt);

    // 获取所有任务，返回Map格式结果
    Map<String, Object> getAllTasks();

    // 获取任务详情，包括关联的图片和描述，返回Map格式结果
    Map<String, Object> getTaskDetails(Integer taskId);
}
