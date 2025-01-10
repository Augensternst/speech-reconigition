package com.example.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service("StudentTaskService")
public interface StudentTaskService {
    @Transactional
    Map<String, Object> submitResponse(Integer studentId, Integer taskId);

    Map<String, Object> getResponsesByTask(Integer taskId);

    @Transactional
    Map<String, Object> gradeResponse(Integer responseId, BigDecimal score, String feedback);
}
