package com.example.backend.service.impl;

import com.example.backend.model.Image;
import com.example.backend.model.Task;
import com.example.backend.repository.ImageRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service("imageService")
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    private final TaskRepository taskRepository;

    public ImageServiceImpl(ImageRepository imageRepository, TaskRepository taskRepository) {
        this.imageRepository = imageRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile image, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String imagePath = request.getServletContext().getRealPath("/upload/");
            saveImage(image, imagePath);
            response.put("success", true);
            response.put("path", imagePath + image.getOriginalFilename());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Image upload failed: " + e.getMessage());
        }
        return response;
    }

    private void saveImage(MultipartFile image, String imagePath) throws IOException {
        File dir = new File(imagePath);
        if (!dir.exists() && dir.mkdir()) {
            throw new IOException("Failed to create directory: " + imagePath);
        }
        File imageFile = new File(imagePath + image.getOriginalFilename());
        image.transferTo(imageFile);
    }

    @Override
    public Map<String, Object> getImagesByTask(Integer taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Task task= taskRepository.findById(taskId).orElse(null);
            if(task == null) {
                throw new Exception("Task not found");
            }
            //Set<Image> images = imageRepository.findByTaskID(task);
            response.put("success", true);
            response.put("data", task);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve images: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Map<String, Object> deleteImage(Integer imageId) {
        Map<String, Object> response = new HashMap<>();
        try {
            imageRepository.deleteById(imageId);
            response.put("success", true);
            response.put("message", "Image deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete image: " + e.getMessage());
        }
        return response;
    }
}
