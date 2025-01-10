package com.example.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiQuestionDTO {
    private String question;
    private List<String> hints;
}
