package com.example.Alotrabong.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface CloudinaryUploadService {
    Map<String, Object> uploadFile(MultipartFile file, String folder);
    Map<String, Object> uploadImage(MultipartFile file, String folder);
    Map<String, Object> uploadVideo(MultipartFile file, String folder);
    boolean deleteFile(String publicId);
}
