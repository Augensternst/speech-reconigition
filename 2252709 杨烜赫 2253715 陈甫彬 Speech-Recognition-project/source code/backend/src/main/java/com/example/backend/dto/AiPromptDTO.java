package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiPromptDTO {
    String systemPrompt;
    String specialPrompt;
    List<String> imageUrl;
}
