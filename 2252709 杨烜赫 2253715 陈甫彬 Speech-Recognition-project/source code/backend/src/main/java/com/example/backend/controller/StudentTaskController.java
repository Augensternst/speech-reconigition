package com.example.backend.controller;


import com.example.backend.service.StudentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/students/{studentId}/tasks")
public class StudentTaskController {

    private final StudentTaskService studentTaskService;

    @Autowired
    public StudentTaskController(@Qualifier("StudentTaskServiceImpl") StudentTaskService studentTaskService) {
        this.studentTaskService = studentTaskService;
    }

    // 学生提交作业
    @PostMapping("/{taskId}/submit")
    public ResponseEntity<Map<String, Object>> submitResponse(@PathVariable Integer studentId,
                                                              @PathVariable Integer taskId) {
        Map<String, Object> result = studentTaskService.submitResponse(studentId, taskId);
        return ResponseEntity.ok(result);
    }

    // 获取作业回复
    @GetMapping("/{taskId}/responses")
    public ResponseEntity<Map<String, Object>> getResponsesByTask(@PathVariable Integer taskId, @PathVariable String studentId) {
        Map<String, Object> response = studentTaskService.getResponsesByTask(taskId);
        return ResponseEntity.ok(response);
    }

    // 评分作业回复
    @PostMapping("/gradeResponse/{responseId}")
    public ResponseEntity<Map<String, Object>> gradeResponse(@PathVariable Integer responseId,
                                                             @RequestBody Map<String, Object> gradingInfo,
                                                             @PathVariable String studentId) {
        BigDecimal score = new BigDecimal(gradingInfo.get("score").toString());
        String feedback = (String) gradingInfo.get("feedback");
        Map<String, Object> result = studentTaskService.gradeResponse(responseId, score, feedback);
        return ResponseEntity.ok(result);
    }
}
