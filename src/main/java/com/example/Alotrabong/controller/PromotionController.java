package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.entity.Promotion;
import com.example.Alotrabong.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Promotion management APIs")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    @Operation(summary = "Get active promotions")
    public ResponseEntity<ApiResponse<List<Promotion>>> getActivePromotions() {
        List<Promotion> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.success("Active promotions retrieved", promotions));
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get promotions by branch")
    public ResponseEntity<ApiResponse<List<Promotion>>> getPromotionsByBranch(@PathVariable String branchId) {
        List<Promotion> promotions = promotionService.getPromotionsByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch promotions retrieved", promotions));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create promotion")
    public ResponseEntity<ApiResponse<Promotion>> createPromotion(@RequestBody Promotion promotion) {
        Promotion created = promotionService.createPromotion(promotion);
        return ResponseEntity.ok(ApiResponse.success("Promotion created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update promotion")
    public ResponseEntity<ApiResponse<Promotion>> updatePromotion(
            @PathVariable String id,
            @RequestBody Promotion promotion) {
        Promotion updated = promotionService.updatePromotion(id, promotion);
        return ResponseEntity.ok(ApiResponse.success("Promotion updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Delete promotion")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable String id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion deleted successfully", null));
    }
}