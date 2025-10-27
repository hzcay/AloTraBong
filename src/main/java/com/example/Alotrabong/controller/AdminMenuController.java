package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.*;
import com.example.Alotrabong.service.AdminCategoryService;
import com.example.Alotrabong.service.ItemService;
import com.example.Alotrabong.service.ItemMediaService;
import com.example.Alotrabong.service.ItemOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
@Tag(name = "Admin Menu", description = "Menu management APIs for admin")
public class AdminMenuController {

    private final ItemService itemService;
    private final AdminCategoryService categoryService;
    private final ItemMediaService itemMediaService;
    private final ItemOptionService itemOptionService;

    // ============= CATEGORY MANAGEMENT =============

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesForDropdown();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", categories));
    }

    @GetMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable String categoryId) {
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", category));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create new category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO created = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", created));
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable String categoryId,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updated = categoryService.updateCategory(categoryId, categoryDTO);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    // ============= ITEM MANAGEMENT =============

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all items")
    public ResponseEntity<ApiResponse<List<ItemDetailDTO>>> getAllItems() {
        List<ItemDTO> items = itemService.getAllItems();
        List<ItemDetailDTO> detailItems = items.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Items retrieved", detailItems));
    }

    @GetMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get item details with media and options")
    public ResponseEntity<ApiResponse<ItemDetailDTO>> getItemById(@PathVariable String itemId) {
        ItemDTO item = itemService.getItemById(itemId);
        ItemDetailDTO detail = convertToDetailDTO(item);
        return ResponseEntity.ok(ApiResponse.success("Item retrieved", detail));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Create new item")
    public ResponseEntity<ApiResponse<ItemDetailDTO>> createItem(@Valid @RequestBody ItemDetailDTO itemDetailDTO) {
        // Create basic item
        ItemDTO itemDTO = ItemDTO.builder()
                .name(itemDetailDTO.getName())
                .description(itemDetailDTO.getDescription())
                .price(itemDetailDTO.getPrice())
                .categoryId(itemDetailDTO.getCategoryId())
                .isActive(itemDetailDTO.getIsActive() != null ? itemDetailDTO.getIsActive() : true)
                .build();

        ItemDTO created = itemService.createItem(itemDTO);

        // Add media
        if (itemDetailDTO.getMedia() != null && !itemDetailDTO.getMedia().isEmpty()) {
            for (ItemMediaDTO mediaDTO : itemDetailDTO.getMedia()) {
                mediaDTO.setItemId(created.getItemId());
                itemMediaService.createMedia(mediaDTO);
            }
        }

        // Add options and their values
        if (itemDetailDTO.getOptions() != null && !itemDetailDTO.getOptions().isEmpty()) {
            for (ItemOptionDTO optionDTO : itemDetailDTO.getOptions()) {
                optionDTO.setItemId(created.getItemId());
                itemOptionService.createOption(optionDTO);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Item created successfully", convertToDetailDTO(created)));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update item")
    public ResponseEntity<ApiResponse<ItemDetailDTO>> updateItem(
            @PathVariable String itemId,
            @Valid @RequestBody ItemDetailDTO itemDetailDTO) {

        ItemDTO itemDTO = ItemDTO.builder()
                .name(itemDetailDTO.getName())
                .description(itemDetailDTO.getDescription())
                .price(itemDetailDTO.getPrice())
                .categoryId(itemDetailDTO.getCategoryId())
                .isActive(itemDetailDTO.getIsActive())
                .build();

        ItemDTO updated = itemService.updateItem(itemId, itemDTO);

        return ResponseEntity.ok(ApiResponse.success("Item updated successfully", convertToDetailDTO(updated)));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete item")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable String itemId) {
        // Delete media and options first
        itemMediaService.deleteAllMediaByItem(itemId);
        itemOptionService.deleteAllOptionsByItem(itemId);
        // Then delete item
        itemService.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully", null));
    }

    @PutMapping("/items/{itemId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Toggle item active status")
    public ResponseEntity<ApiResponse<ItemDetailDTO>> toggleItemStatus(@PathVariable String itemId) {
        ItemDTO updated = itemService.activateItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Item status updated", convertToDetailDTO(updated)));
    }

    // ============= ITEM MEDIA MANAGEMENT =============

    @PostMapping("/items/{itemId}/media")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Add media to item")
    public ResponseEntity<ApiResponse<ItemMediaDTO>> addMedia(
            @PathVariable String itemId,
            @Valid @RequestBody ItemMediaDTO mediaDTO) {
        mediaDTO.setItemId(itemId);
        ItemMediaDTO created = itemMediaService.createMedia(mediaDTO);
        return ResponseEntity.ok(ApiResponse.success("Media added successfully", created));
    }

    @GetMapping("/items/{itemId}/media")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all media for item")
    public ResponseEntity<ApiResponse<List<ItemMediaDTO>>> getItemMedia(@PathVariable String itemId) {
        List<ItemMediaDTO> media = itemMediaService.getMediaByItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Media retrieved", media));
    }

    @GetMapping("/media/{mediaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get media by ID")
    public ResponseEntity<ApiResponse<ItemMediaDTO>> getMediaById(@PathVariable Integer mediaId) {
        ItemMediaDTO media = itemMediaService.getMediaById(mediaId);
        return ResponseEntity.ok(ApiResponse.success("Media retrieved", media));
    }

    @PutMapping("/media/{mediaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update media")
    public ResponseEntity<ApiResponse<ItemMediaDTO>> updateMedia(
            @PathVariable Integer mediaId,
            @Valid @RequestBody ItemMediaDTO mediaDTO) {
        ItemMediaDTO updated = itemMediaService.updateMedia(mediaId, mediaDTO);
        return ResponseEntity.ok(ApiResponse.success("Media updated successfully", updated));
    }

    @DeleteMapping("/media/{mediaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Delete media")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable Integer mediaId) {
        itemMediaService.deleteMedia(mediaId);
        return ResponseEntity.ok(ApiResponse.success("Media deleted successfully", null));
    }

    // ============= ITEM OPTION MANAGEMENT =============

    @PostMapping("/items/{itemId}/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Add option to item")
    public ResponseEntity<ApiResponse<ItemOptionDTO>> addOption(
            @PathVariable String itemId,
            @Valid @RequestBody ItemOptionDTO optionDTO) {
        optionDTO.setItemId(itemId);
        ItemOptionDTO created = itemOptionService.createOption(optionDTO);
        return ResponseEntity.ok(ApiResponse.success("Option added successfully", created));
    }

    @GetMapping("/items/{itemId}/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all options for item")
    public ResponseEntity<ApiResponse<List<ItemOptionDTO>>> getItemOptions(@PathVariable String itemId) {
        List<ItemOptionDTO> options = itemOptionService.getOptionsByItem(itemId);
        return ResponseEntity.ok(ApiResponse.success("Options retrieved", options));
    }

    @GetMapping("/options/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get option by ID")
    public ResponseEntity<ApiResponse<ItemOptionDTO>> getOptionById(@PathVariable Integer optionId) {
        ItemOptionDTO option = itemOptionService.getOptionById(optionId);
        return ResponseEntity.ok(ApiResponse.success("Option retrieved", option));
    }

    @PutMapping("/options/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update option")
    public ResponseEntity<ApiResponse<ItemOptionDTO>> updateOption(
            @PathVariable Integer optionId,
            @Valid @RequestBody ItemOptionDTO optionDTO) {
        ItemOptionDTO updated = itemOptionService.updateOption(optionId, optionDTO);
        return ResponseEntity.ok(ApiResponse.success("Option updated successfully", updated));
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Delete option")
    public ResponseEntity<ApiResponse<Void>> deleteOption(@PathVariable Integer optionId) {
        itemOptionService.deleteOption(optionId);
        return ResponseEntity.ok(ApiResponse.success("Option deleted successfully", null));
    }

    // ============= OPTION VALUE MANAGEMENT =============

    @PostMapping("/options/{optionId}/values")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Add value to option")
    public ResponseEntity<ApiResponse<ItemOptionValueDTO>> addOptionValue(
            @PathVariable Integer optionId,
            @Valid @RequestBody ItemOptionValueDTO valueDTO) {
        valueDTO.setOptionId(optionId);
        ItemOptionValueDTO created = itemOptionService.createOptionValue(valueDTO);
        return ResponseEntity.ok(ApiResponse.success("Option value added successfully", created));
    }

    @GetMapping("/options/{optionId}/values")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all values for option")
    public ResponseEntity<ApiResponse<List<ItemOptionValueDTO>>> getOptionValues(@PathVariable Integer optionId) {
        List<ItemOptionValueDTO> values = itemOptionService.getValuesByOption(optionId);
        return ResponseEntity.ok(ApiResponse.success("Option values retrieved", values));
    }

    @GetMapping("/option-values/{valueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get option value by ID")
    public ResponseEntity<ApiResponse<ItemOptionValueDTO>> getOptionValueById(@PathVariable Integer valueId) {
        ItemOptionValueDTO value = itemOptionService.getOptionValueById(valueId);
        return ResponseEntity.ok(ApiResponse.success("Option value retrieved", value));
    }

    @PutMapping("/option-values/{valueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Update option value")
    public ResponseEntity<ApiResponse<ItemOptionValueDTO>> updateOptionValue(
            @PathVariable Integer valueId,
            @Valid @RequestBody ItemOptionValueDTO valueDTO) {
        ItemOptionValueDTO updated = itemOptionService.updateOptionValue(valueId, valueDTO);
        return ResponseEntity.ok(ApiResponse.success("Option value updated successfully", updated));
    }

    @DeleteMapping("/option-values/{valueId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Delete option value")
    public ResponseEntity<ApiResponse<Void>> deleteOptionValue(@PathVariable Integer valueId) {
        itemOptionService.deleteOptionValue(valueId);
        return ResponseEntity.ok(ApiResponse.success("Option value deleted successfully", null));
    }

    // Helper method to convert ItemDTO to ItemDetailDTO with media and options
    private ItemDetailDTO convertToDetailDTO(ItemDTO itemDTO) {
        List<ItemMediaDTO> media = itemMediaService.getMediaByItem(itemDTO.getItemId());
        List<ItemOptionDTO> options = itemOptionService.getOptionsByItem(itemDTO.getItemId());

        return ItemDetailDTO.builder()
                .itemId(itemDTO.getItemId())
                .itemCode(itemDTO.getName())
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .price(itemDTO.getPrice())
                .categoryId(itemDTO.getCategoryId())
                .categoryName(itemDTO.getCategoryName())
                .isActive(itemDTO.getIsActive())
                .media(media)
                .options(options)
                .createdAt(itemDTO.getCreatedAt())
                .updatedAt(itemDTO.getUpdatedAt())
                .build();
    }
}

