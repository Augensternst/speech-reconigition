package com.example.backend.controller;

import com.example.backend.dto.AiQuestionDTO;
import com.example.backend.dto.ChildResponseDTO;
import com.example.backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/questions")
    public ResponseEntity<Object> getAiQuestions(@RequestBody ChildResponseDTO childResponse) {
        try {
            List<AiQuestionDTO> aiResponse = aiService.getAiQuestions(childResponse);
            return ResponseEntity.ok(aiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/conversation")
    public ResponseEntity<Map<String, Object>> getConversation(@RequestParam Integer taskId) {
        Map<String, Object> conversation = aiService.getConversation(taskId);

        // 检查 "succeeded" 的值
        if (Boolean.FALSE.equals(conversation.get("succeeded"))) {
            return ResponseEntity.status(500).body(conversation);
        }
        return ResponseEntity.ok(conversation);
    }
}
