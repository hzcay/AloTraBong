package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.BranchDTO;
import com.example.Alotrabong.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Branch", description = "Branch management APIs")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Get all branches")
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches() {
        List<BranchDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved", branches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(@PathVariable String id) {
        BranchDTO branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved", branch));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new branch")
    public ResponseEntity<ApiResponse<BranchDTO>> createBranch(@Valid @RequestBody BranchDTO branchDTO) {
        BranchDTO created = branchService.createBranch(branchDTO);
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(
            @PathVariable String id,
            @Valid @RequestBody BranchDTO branchDTO) {
        BranchDTO updated = branchService.updateBranch(id, branchDTO);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete branch")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable String id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}