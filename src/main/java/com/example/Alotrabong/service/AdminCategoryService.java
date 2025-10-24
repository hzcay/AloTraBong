package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminCategoryService {

    // Category Management
    Page<CategoryDTO> getAllCategories(Pageable pageable);

    CategoryDTO getCategoryById(String categoryId);

    CategoryDTO createCategory(CategoryDTO dto);

    CategoryDTO updateCategory(String categoryId, CategoryDTO dto);

    void deleteCategory(String categoryId);

    List<CategoryDTO> getAllCategoriesForDropdown();

    long getTotalCategoriesCount();
}

