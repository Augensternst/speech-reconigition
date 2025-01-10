package com.example.backend.controller;



import com.example.backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping("/getAllTasks")
    public ResponseEntity<Map<String, Object>> getAllTasks() {
        Map<String, Object> response = taskService.getAllTasks();
            return ResponseEntity.ok(response);
    }

    @GetMapping("/getTaskDetails")
    public ResponseEntity<Map<String, Object>> getTaskDetails(@RequestParam("taskId") Integer taskId) {
        Map<String, Object> response = taskService.getTaskDetails(taskId);
        return response.get("success").equals(true)
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/createTask")
    public ResponseEntity<Map<String, Object>> createTask(@RequestBody Map<String,Object> taskPrompt) {
        Map<String, Object> response = taskService.createTask(taskPrompt);
        return response.get("success").equals(true)
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
