package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.*;
import com.example.Alotrabong.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminApiController {

    private final AdminUserService adminUserService;
    private final AdminBranchService adminBranchService;
    private final AdminCategoryService adminCategoryService;
    private final AdminPromotionService adminPromotionService;
    private final AdminShippingService adminShippingService;
    private final AdminReportService adminReportService;

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public ResponseEntity<Page<UserManagementDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserManagementDTO> users = adminUserService.getAllUsers(pageable, search);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementDTO> getUserById(@PathVariable String userId) {
        log.info("Fetching user by id: {}", userId);
        UserManagementDTO user = adminUserService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<Map<String, String>> lockUser(@PathVariable String userId) {
        log.info("Locking user: {}", userId);
        adminUserService.lockUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User locked successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(@PathVariable String userId) {
        log.info("Unlocking user: {}", userId);
        adminUserService.unlockUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User unlocked successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        log.info("Resetting password for user: {}", userId);
        String newPassword = request.get("newPassword");
        adminUserService.resetUserPassword(userId, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}/assign-role")
    public ResponseEntity<Map<String, String>> assignRoleToUser(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        log.info("Assigning role to user: {}", userId);
        String roleCode = request.get("roleCode");
        adminUserService.assignRoleToUser(userId, roleCode);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role assigned successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/remove-role")
    public ResponseEntity<Map<String, String>> removeRoleFromUser(
            @PathVariable String userId,
            @RequestParam String roleCode) {
        log.info("Removing role from user: {}", userId);
        adminUserService.removeRoleFromUser(userId, roleCode);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role removed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/role/{roleCode}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String roleCode) {
        log.info("Fetching users by role: {}", roleCode);
        return ResponseEntity.ok(adminUserService.getUsersByRole(roleCode));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        log.info("Deleting user: {}", userId);
        adminUserService.deleteUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserManagementDTO> updateUserInfo(
            @PathVariable String userId,
            @RequestBody UserManagementDTO dto) {
        log.info("Updating user info: {}", userId);
        UserManagementDTO updatedUser = adminUserService.updateUserInfo(userId, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/stats/summary")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        log.info("Fetching user statistics");
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", adminUserService.getTotalUsersCount());
        stats.put("activeUsers", adminUserService.getActiveUsersCount());
        stats.put("lockedUsers", adminUserService.getLockedUsersCount());
        return ResponseEntity.ok(stats);
    }

    // ==================== BRANCH MANAGEMENT ====================

    @GetMapping("/branches")
    public ResponseEntity<Page<BranchManagementDTO>> getAllBranches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.info("Fetching all branches - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<BranchManagementDTO> branches = adminBranchService.getAllBranches(pageable, search);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/branches/{branchId}")
    public ResponseEntity<BranchManagementDTO> getBranchById(@PathVariable String branchId) {
        log.info("Fetching branch by id: {}", branchId);
        BranchManagementDTO branch = adminBranchService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    @PostMapping("/branches")
    public ResponseEntity<BranchManagementDTO> createBranch(@RequestBody BranchManagementDTO dto) {
        log.info("Creating new branch: {}", dto.getName());
        BranchManagementDTO branch = adminBranchService.createBranch(dto);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/branches/{branchId}")
    public ResponseEntity<BranchManagementDTO> updateBranch(
            @PathVariable String branchId,
            @RequestBody BranchManagementDTO dto) {
        log.info("Updating branch: {}", branchId);
        BranchManagementDTO branch = adminBranchService.updateBranch(branchId, dto);
        return ResponseEntity.ok(branch);
    }

    @PutMapping("/branches/{branchId}/activate")
    public ResponseEntity<Map<String, String>> activateBranch(@PathVariable String branchId) {
        log.info("Activating branch: {}", branchId);
        adminBranchService.activateBranch(branchId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Branch activated successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/branches/{branchId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateBranch(@PathVariable String branchId) {
        log.info("Deactivating branch: {}", branchId);
        adminBranchService.deactivateBranch(branchId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Branch deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/branches/{branchId}/manager/{userId}")
    public ResponseEntity<Map<String, String>> assignBranchManager(
            @PathVariable String branchId,
            @PathVariable String userId) {
        log.info("Assigning manager {} to branch: {}", userId, branchId);
        adminBranchService.assignBranchManager(branchId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Branch manager assigned successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/branches/{branchId}/manager")
    public ResponseEntity<Map<String, String>> removeBranchManager(@PathVariable String branchId) {
        log.info("Removing manager from branch: {}", branchId);
        adminBranchService.removeBranchManager(branchId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Branch manager removed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/branches/stats/summary")
    public ResponseEntity<Map<String, Long>> getBranchStats() {
        log.info("Fetching branch statistics");
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalBranches", adminBranchService.getTotalBranchesCount());
        stats.put("activeBranches", adminBranchService.getActiveBranchesCount());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/branches/stats/total")
    public ResponseEntity<Long> getTotalBranchesCount() {
        log.info("Fetching total branches count");
        return ResponseEntity.ok(adminBranchService.getTotalBranchesCount());
    }

    @GetMapping("/branches/stats/active")
    public ResponseEntity<Long> getActiveBranchesCount() {
        log.info("Fetching active branches count");
        return ResponseEntity.ok(adminBranchService.getActiveBranchesCount());
    }

    @GetMapping("/branches/all")
    public ResponseEntity<List<BranchManagementDTO>> getAllBranchesForDropdown() {
        log.info("Fetching all branches for dropdown");
        List<BranchManagementDTO> branches = adminBranchService.getAllBranchesForDropdown();
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/users/shippers")
    public ResponseEntity<List<UserManagementDTO>> getShipperUsers() {
        log.info("Fetching users with SHIPPER role");
        List<UserManagementDTO> users = adminUserService.getUsersByRole("SHIPPER");
        return ResponseEntity.ok(users);
    }

    // ==================== CATEGORY MANAGEMENT ====================

    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all categories - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDTO> categories = adminCategoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String categoryId) {
        log.info("Fetching category by id: {}", categoryId);
        CategoryDTO category = adminCategoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO dto) {
        log.info("Creating new category: {}", dto.getName());
        CategoryDTO category = adminCategoryService.createCategory(dto);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable String categoryId,
            @RequestBody CategoryDTO dto) {
        log.info("Updating category: {}", categoryId);
        CategoryDTO category = adminCategoryService.updateCategory(categoryId, dto);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String categoryId) {
        log.info("Deleting category: {}", categoryId);
        adminCategoryService.deleteCategory(categoryId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Category deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories/dropdown")
    public ResponseEntity<?> getCategoriesForDropdown() {
        log.info("Fetching categories for dropdown");
        return ResponseEntity.ok(adminCategoryService.getAllCategoriesForDropdown());
    }

    // ==================== PROMOTION MANAGEMENT ====================

    @GetMapping("/promotions")
    public ResponseEntity<Page<PromotionManagementDTO>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        log.info("Fetching all promotions - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionManagementDTO> promotions = adminPromotionService.getAllPromotions(pageable, search);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/{promotionId}")
    public ResponseEntity<PromotionManagementDTO> getPromotionById(@PathVariable String promotionId) {
        log.info("Fetching promotion by id: {}", promotionId);
        PromotionManagementDTO promotion = adminPromotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(promotion);
    }

    @PostMapping("/promotions")
    public ResponseEntity<PromotionManagementDTO> createPromotion(@RequestBody PromotionManagementDTO dto) {
        log.info("Creating new promotion: {}", dto.getCode());
        PromotionManagementDTO promotion = adminPromotionService.createPromotion(dto);
        return ResponseEntity.ok(promotion);
    }

    @PutMapping("/promotions/{promotionId}")
    public ResponseEntity<PromotionManagementDTO> updatePromotion(
            @PathVariable String promotionId,
            @RequestBody PromotionManagementDTO dto) {
        log.info("Updating promotion: {}", promotionId);
        PromotionManagementDTO promotion = adminPromotionService.updatePromotion(promotionId, dto);
        return ResponseEntity.ok(promotion);
    }

    @DeleteMapping("/promotions/{promotionId}")
    public ResponseEntity<Map<String, String>> deletePromotion(@PathVariable String promotionId) {
        log.info("Deleting promotion: {}", promotionId);
        adminPromotionService.deletePromotion(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promotions/{promotionId}/activate")
    public ResponseEntity<Map<String, String>> activatePromotion(@PathVariable String promotionId) {
        log.info("Activating promotion: {}", promotionId);
        adminPromotionService.activatePromotion(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion activated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/promotions/{promotionId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivatePromotion(@PathVariable String promotionId) {
        log.info("Deactivating promotion: {}", promotionId);
        adminPromotionService.deactivatePromotion(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Promotion deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/promotions/stats/summary")
    public ResponseEntity<Map<String, Long>> getPromotionStats() {
        log.info("Fetching promotion statistics");
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalPromotions", adminPromotionService.getTotalPromotionsCount());
        stats.put("activePromotions", adminPromotionService.getActivePromotionsCount());
        return ResponseEntity.ok(stats);
    }

    // ==================== SHIPPING RATE MANAGEMENT ====================

    @GetMapping("/shipping-rates")
    public ResponseEntity<Page<ShippingRateDTO>> getAllShippingRates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all shipping rates - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ShippingRateDTO> rates = adminShippingService.getAllShippingRates(pageable);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/shipping-rates/{rateId}")
    public ResponseEntity<ShippingRateDTO> getShippingRateById(@PathVariable Integer rateId) {
        log.info("Fetching shipping rate by id: {}", rateId);
        ShippingRateDTO rate = adminShippingService.getShippingRateById(rateId);
        return ResponseEntity.ok(rate);
    }

    @PostMapping("/shipping-rates")
    public ResponseEntity<ShippingRateDTO> createShippingRate(@RequestBody ShippingRateDTO dto) {
        log.info("Creating new shipping rate");
        ShippingRateDTO rate = adminShippingService.createShippingRate(dto);
        return ResponseEntity.ok(rate);
    }

    @PutMapping("/shipping-rates/{rateId}")
    public ResponseEntity<ShippingRateDTO> updateShippingRate(
            @PathVariable Integer rateId,
            @RequestBody ShippingRateDTO dto) {
        log.info("Updating shipping rate: {}", rateId);
        ShippingRateDTO rate = adminShippingService.updateShippingRate(rateId, dto);
        return ResponseEntity.ok(rate);
    }

    @DeleteMapping("/shipping-rates/{rateId}")
    public ResponseEntity<Map<String, String>> deleteShippingRate(@PathVariable Integer rateId) {
        log.info("Deleting shipping rate: {}", rateId);
        adminShippingService.deleteShippingRate(rateId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Shipping rate deleted successfully");
        return ResponseEntity.ok(response);
    }


    // ==================== REPORTS & STATISTICS ====================

    @GetMapping("/reports/revenue-by-branch")
    public ResponseEntity<Page<RevenueReportDTO>> getRevenueByBranch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Fetching revenue by branch");
        Pageable pageable = PageRequest.of(page, size);
        Page<RevenueReportDTO> reports = adminReportService.getRevenueByBranch(pageable, null, null);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reports/revenue-by-item")
    public ResponseEntity<Page<RevenueReportDTO>> getRevenueByItem(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Fetching revenue by item");
        Pageable pageable = PageRequest.of(page, size);
        Page<RevenueReportDTO> reports = adminReportService.getRevenueByItem(pageable, null, null);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reports/system-statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Fetching system statistics");
        Map<String, Object> stats = new HashMap<>();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/dashboard-summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        log.info("Fetching dashboard summary");
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", adminUserService.getTotalUsersCount());
        summary.put("activeUsers", adminUserService.getActiveUsersCount());
        summary.put("totalBranches", adminBranchService.getTotalBranchesCount());
        summary.put("activeBranches", adminBranchService.getActiveBranchesCount());
        summary.put("totalPromotions", adminPromotionService.getTotalPromotionsCount());
        summary.put("activePromotions", adminPromotionService.getActivePromotionsCount());
        return ResponseEntity.ok(summary);
    }
}

