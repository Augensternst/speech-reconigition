package com.example.backend.service.impl;

import com.example.backend.model.StudentResponse;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.StudentResponseRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.StudentTaskService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("StudentTaskServiceImpl")
public class StudentTaskServiceImpl implements StudentTaskService {

    private final StudentResponseRepository responseRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudentTaskServiceImpl(StudentResponseRepository responseRepository,
                                  TaskRepository taskRepository,
                                  UserRepository userRepository) {
        this.responseRepository = responseRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * 学生提交任务
     * TODO: 目前学生不在任务对应的Course中也可以开始任务。
     */
    @Transactional
    @Override
    public Map<String, Object> submitResponse(Integer studentId, Integer taskId) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Optional<Task> task = taskRepository.findById(taskId);
            if (task.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "无效的任务 ID");
                return responseMap;
            }

            Optional<User> student = userRepository.findById(studentId);
            if (student.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "无效的学生或任务 ID");
                return responseMap;
            }

            StudentResponse response = taskBegin(student.get(), task.get());
            responseRepository.save(response);

            responseMap.put("success", true);
            responseMap.put("message", "提交成功");
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "提交失败: " + e.getMessage());
        }
        return responseMap;
    }

    /**
     * 获取某任务的所有学生提交
     */
    @Override
    public Map<String, Object> getResponsesByTask(Integer taskId) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            List<StudentResponse> responses = responseRepository.findByTaskId(taskId);
            responseMap.put("success", true);
            responseMap.put("responses", responses);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "获取提交失败: " + e.getMessage());
        }
        return responseMap;
    }

    /**
     * 教师对学生提交进行评分和反馈
     */
    @Transactional
    @Override
    public Map<String, Object> gradeResponse(Integer responseId, BigDecimal score, String feedback) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Optional<StudentResponse> responseOpt = responseRepository.findById(responseId);

            if (responseOpt.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "提交记录不存在");
                return responseMap;
            }

            StudentResponse response = responseOpt.get();
            response.setAiFeedback(Map.of("feedback", feedback));
            responseRepository.save(response);

            responseMap.put("success", true);
            responseMap.put("message", "评分成功");
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "评分失败: " + e.getMessage());
        }
        return responseMap;
    }

    private StudentResponse taskBegin(User student, Task task) {
        StudentResponse response = new StudentResponse();
        response.setStudent(student);
        response.setTask(task);
        response.setResponseText("responseText");
        response.setSubmissionDate(Instant.ofEpochSecond(Instant.now().getEpochSecond(), 0));
        return response;
    }
}
