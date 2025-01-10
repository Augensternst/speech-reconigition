package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChildResponseDTO {
    private Integer studentId;
    private String response;
    private Integer phase;
    private Integer taskId;
}
