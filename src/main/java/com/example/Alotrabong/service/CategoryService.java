package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    
    CategoryDTO getCategoryById(String categoryId);
    
    List<CategoryDTO> getAllCategories();
    
    CategoryDTO updateCategory(String categoryId, CategoryDTO categoryDTO);
    
    void deleteCategory(String categoryId);
}
