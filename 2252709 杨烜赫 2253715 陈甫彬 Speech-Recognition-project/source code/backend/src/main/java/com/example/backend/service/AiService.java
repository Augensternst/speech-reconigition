package com.example.backend.service;

import com.example.backend.dto.AiQuestionDTO;
import com.example.backend.dto.ChildResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface AiService {
    @Transactional
    List<AiQuestionDTO> getAiQuestions(ChildResponseDTO childResponse) throws Exception;

    Map<String, Object> getConversation(Integer taskId);
}
