package com.example.Alotrabong.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Alotrabong.service.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryUploadServiceImpl implements CloudinaryUploadService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String folder) {
        try {
            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new IllegalArgumentException("Không thể xác định loại file");
            }

            // Tạo options cho upload
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto", // Tự động phát hiện loại file
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            // Upload file
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            
            log.info("File uploaded successfully: {}", result.get("public_id"));
            return result;

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        try {
            // Kiểm tra loại file có phải ảnh không
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File phải là ảnh");
            }

            // Tạo options cho upload ảnh
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            // Upload ảnh
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            
            log.info("Image uploaded successfully: {}", result.get("public_id"));
            return result;

        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> uploadVideo(MultipartFile file, String folder) {
        try {
            // Kiểm tra loại file có phải video không
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IllegalArgumentException("File phải là video");
            }

            // Tạo options cho upload video
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "video",
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", false
            );

            // Upload video
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            
            log.info("Video uploaded successfully: {}", result.get("public_id"));
            return result;

        } catch (IOException e) {
            log.error("Error uploading video: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi upload video: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean deleted = (Boolean) result.get("result");
            
            if (deleted) {
                log.info("File deleted successfully: {}", publicId);
            } else {
                log.warn("File deletion failed: {}", publicId);
            }
            
            return deleted;

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return false;
        }
    }
}
