package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.CategoryDTO;
import com.example.Alotrabong.entity.Category;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.CategoryRepository;
import com.example.Alotrabong.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());
        
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .isActive(true)
                .build();
        
        category = categoryRepository.save(category);
        log.info("Category created successfully: {}", category.getCategoryId());
        
        return convertToDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return convertToDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(String categoryId, CategoryDTO categoryDTO) {
        log.info("Updating category: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        
        category = categoryRepository.save(category);
        log.info("Category updated successfully: {}", categoryId);
        
        return convertToDTO(category);
    }

    @Override
    public void deleteCategory(String categoryId) {
        log.info("Deleting category: {}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        category.setIsActive(false);
        categoryRepository.save(category);
        
        log.info("Category deactivated: {}", categoryId);
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
