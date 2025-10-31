package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ItemService {
    
    ItemDTO createItem(ItemDTO itemDTO);
    
    ItemDTO getItemById(String itemId);
    
    List<ItemDTO> getAllItems();
    
    Page<ItemDTO> getItemsByCategory(String categoryId, Pageable pageable);
    
    List<ItemDTO> getItemsByBranch(String branchId);
    
    List<ItemDTO> searchItems(String keyword);
    
    List<ItemDTO> getTopSellingItems(int limit);
    
    List<ItemDTO> getNewItems(int limit);
    
    ItemDTO updateItem(String itemId, ItemDTO itemDTO);
    
    void deleteItem(String itemId);
    
    ItemDTO activateItem(String itemId);
    
    ItemDTO setBranchPrice(String itemId, String branchId, BigDecimal price);


    /** Top được yêu thích (toàn hệ thống) */
    List<ItemDTO> getTopFavoritedItems(int limit);


}