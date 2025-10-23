package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.ReviewDTO;
import com.example.Alotrabong.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Get reviews by item")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getReviewsByItem(@PathVariable String itemId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user's reviews")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getReviewsByUser(Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        List<ReviewDTO> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User reviews retrieved", reviews));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create review")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        reviewDTO.setUserId(userId);
        ReviewDTO created = reviewService.createReview(reviewDTO);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update review")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateReview(
            @PathVariable String id,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        reviewDTO.setUserId(userId);
        ReviewDTO updated = reviewService.updateReview(id, reviewDTO);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }

    private String getUserIdFromAuth(Authentication authentication) {
        // TODO: Implement based on your JWT authentication setup
        return "user-id-placeholder"; // Placeholder
    }
}