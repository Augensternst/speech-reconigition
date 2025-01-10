package com.example.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片服务类：负责图片的上传和管理
 */
@Service("ImageService")
public interface ImageService {
    // 上传图片，返回Map格式结果
    Map<String, Object> uploadImage(MultipartFile image, HttpServletRequest request);

    // 获取某任务的所有图片，返回Map格式结果
    Map<String, Object> getImagesByTask(Integer taskId);

    // 删除指定图片，返回Map格式结果
    Map<String, Object> deleteImage(Integer imageId);
}
