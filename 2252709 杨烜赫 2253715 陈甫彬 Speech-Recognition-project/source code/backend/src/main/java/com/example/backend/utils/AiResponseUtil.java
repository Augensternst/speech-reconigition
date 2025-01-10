package com.example.backend.utils;

import com.example.backend.dto.AiPromptDTO;

public interface AiResponseUtil {
    String generateAiQuestion(AiPromptDTO inputPrompt) throws Exception;
}
