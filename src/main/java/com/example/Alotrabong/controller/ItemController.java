package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.ItemDTO;
import com.example.Alotrabong.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "Item management APIs")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get all items")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getAllItems() {
        List<ItemDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(ApiResponse.success("Items retrieved", items));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ApiResponse<ItemDTO>> getItemById(@PathVariable String id) {
        ItemDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item retrieved", item));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Get top selling items")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getTopSellingItems(
            @RequestParam(defaultValue = "10") int limit) {
        List<ItemDTO> items = itemService.getTopSellingItems(limit);
        return ResponseEntity.ok(ApiResponse.success("Top selling items retrieved", items));
    }

    @GetMapping("/new")
    @Operation(summary = "Get new items")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getNewItems(
            @RequestParam(defaultValue = "10") int limit) {
        List<ItemDTO> items = itemService.getNewItems(limit);
        return ResponseEntity.ok(ApiResponse.success("New items retrieved", items));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get items by category")
    public ResponseEntity<ApiResponse<Page<ItemDTO>>> getItemsByCategory(
            @PathVariable String categoryId,
            Pageable pageable) {
        Page<ItemDTO> items = itemService.getItemsByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Items by category retrieved", items));
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get items by branch")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> getItemsByBranch(@PathVariable String branchId) {
        List<ItemDTO> items = itemService.getItemsByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Items by branch retrieved", items));
    }

    @GetMapping("/search")
    @Operation(summary = "Search items by keyword")
    public ResponseEntity<ApiResponse<List<ItemDTO>>> searchItems(@RequestParam String keyword) {
        List<ItemDTO> items = itemService.searchItems(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved", items));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create new item")
    public ResponseEntity<ApiResponse<ItemDTO>> createItem(@Valid @RequestBody ItemDTO itemDTO) {
        ItemDTO created = itemService.createItem(itemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update item")
    public ResponseEntity<ApiResponse<ItemDTO>> updateItem(
            @PathVariable String id,
            @Valid @RequestBody ItemDTO itemDTO) {
        ItemDTO updated = itemService.updateItem(id, itemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete item")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Activate item")
    public ResponseEntity<ApiResponse<ItemDTO>> activateItem(@PathVariable String id) {
        ItemDTO item = itemService.activateItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item activated successfully", item));
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Set item price for branch")
    public ResponseEntity<ApiResponse<ItemDTO>> setBranchPrice(
            @PathVariable String id,
            @RequestParam String branchId,
            @RequestParam BigDecimal price) {
        ItemDTO item = itemService.setBranchPrice(id, branchId, price);
        return ResponseEntity.ok(ApiResponse.success("Branch price set successfully", item));
    }
}
