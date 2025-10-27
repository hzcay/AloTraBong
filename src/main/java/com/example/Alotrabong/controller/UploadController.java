package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.service.CloudinaryUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Upload", description = "File upload APIs for admin")
public class UploadController {

    private final CloudinaryUploadService cloudinaryUploadService;

    @PostMapping("/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Upload image file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "alotrabong/images") String folder) {
        
        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            // Kiểm tra kích thước file (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File quá lớn. Kích thước tối đa là 10MB"));
            }

            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải là ảnh (JPG, PNG, GIF, etc.)"));
            }

            // Upload ảnh
            Map<String, Object> result = cloudinaryUploadService.uploadImage(file, folder);
            
            // Tạo response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", result.get("secure_url"));
            responseData.put("publicId", result.get("public_id"));
            responseData.put("format", result.get("format"));
            responseData.put("width", result.get("width"));
            responseData.put("height", result.get("height"));
            responseData.put("size", result.get("bytes"));

            log.info("Image uploaded successfully: {}", result.get("public_id"));
            return ResponseEntity.ok(ApiResponse.success("Upload ảnh thành công", responseData));

        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi upload ảnh: " + e.getMessage()));
        }
    }

    @PostMapping("/video")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Upload video file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "alotrabong/videos") String folder) {
        
        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            // Kiểm tra kích thước file (max 100MB)
            if (file.getSize() > 100 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File quá lớn. Kích thước tối đa là 100MB"));
            }

            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải là video (MP4, AVI, MOV, etc.)"));
            }

            // Upload video
            Map<String, Object> result = cloudinaryUploadService.uploadVideo(file, folder);
            
            // Tạo response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", result.get("secure_url"));
            responseData.put("publicId", result.get("public_id"));
            responseData.put("format", result.get("format"));
            responseData.put("duration", result.get("duration"));
            responseData.put("size", result.get("bytes"));

            log.info("Video uploaded successfully: {}", result.get("public_id"));
            return ResponseEntity.ok(ApiResponse.success("Upload video thành công", responseData));

        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi upload video: " + e.getMessage()));
        }
    }

    @PostMapping("/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Upload any file type")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "alotrabong/files") String folder) {
        
        try {
            // Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            // Kiểm tra kích thước file (max 50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File quá lớn. Kích thước tối đa là 50MB"));
            }

            // Upload file
            Map<String, Object> result = cloudinaryUploadService.uploadFile(file, folder);
            
            // Tạo response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", result.get("secure_url"));
            responseData.put("publicId", result.get("public_id"));
            responseData.put("format", result.get("format"));
            responseData.put("resourceType", result.get("resource_type"));
            responseData.put("size", result.get("bytes"));

            log.info("File uploaded successfully: {}", result.get("public_id"));
            return ResponseEntity.ok(ApiResponse.success("Upload file thành công", responseData));

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi upload file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/file/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Delete file from Cloudinary")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String publicId) {
        try {
            boolean deleted = cloudinaryUploadService.deleteFile(publicId);
            
            if (deleted) {
                log.info("File deleted successfully: {}", publicId);
                return ResponseEntity.ok(ApiResponse.success("Xóa file thành công", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không thể xóa file"));
            }

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi khi xóa file: " + e.getMessage()));
        }
    }
}
