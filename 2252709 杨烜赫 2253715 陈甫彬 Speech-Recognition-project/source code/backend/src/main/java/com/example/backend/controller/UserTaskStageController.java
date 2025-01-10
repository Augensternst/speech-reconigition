package com.example.backend.controller;

import com.example.backend.model.Task;
import com.example.backend.service.UserTaskStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserTaskStageController {

    private final UserTaskStageService userTaskStageService;

    @Autowired
    public UserTaskStageController(UserTaskStageService userTaskStageService) {
        this.userTaskStageService = userTaskStageService;
    }

    @GetMapping("/getStage")
    public ResponseEntity<Integer> getStage(Integer userId, Integer taskId) {
        return ResponseEntity.ok(userTaskStageService.getStage(userId, taskId));
    }

    @GetMapping("/getTasks")
    public ResponseEntity<List<Task>> getTasks(Integer userId) {
        return ResponseEntity.ok(userTaskStageService.getTasks(userId));
    }

    @PostMapping("/updateStage")
    public ResponseEntity<String> updateStage(Integer userId, Integer taskId, Integer stage) {
        try {
            userTaskStageService.updateStage(userId, taskId, stage);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
