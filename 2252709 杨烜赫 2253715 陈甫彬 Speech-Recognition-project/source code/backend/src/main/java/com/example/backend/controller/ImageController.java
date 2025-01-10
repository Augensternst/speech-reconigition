package com.example.backend.controller;


import com.example.backend.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile image
                                                            , HttpServletRequest request) {
        Map<String, Object> response = imageService.uploadImage(image, request);
        return response.get("success").equals(true)
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/getImagesByTask")
    public ResponseEntity<Map<String, Object>> getImagesByTask(@RequestParam("taskId") Integer taskId) {
        Map<String, Object> response = imageService.getImagesByTask(taskId);
        return response.get("success").equals(true)
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
