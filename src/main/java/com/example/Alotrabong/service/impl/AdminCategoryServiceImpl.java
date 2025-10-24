package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.CategoryDTO;
import com.example.Alotrabong.entity.Category;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.CategoryRepository;
import com.example.Alotrabong.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        log.info("Fetching all categories");
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(this::convertToDTO);
    }

    @Override
    public CategoryDTO getCategoryById(String categoryId) {
        log.info("Fetching category by id: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        log.info("Creating new category: {}", dto.getName());
        Category category = Category.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(true)
                .build();

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO updateCategory(String categoryId, CategoryDTO dto) {
        log.info("Updating category: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsActive(dto.getIsActive());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    public void deleteCategory(String categoryId) {
        log.info("Deleting category: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryDTO> getAllCategoriesForDropdown() {
        log.info("Fetching all active categories for dropdown");
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getTotalCategoriesCount() {
        return categoryRepository.count();
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .parentId(category.getParent() != null ? category.getParent().getCategoryId() : null)
                .build();
    }
}

