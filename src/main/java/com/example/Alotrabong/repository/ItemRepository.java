package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Category;
import com.example.Alotrabong.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    
    Page<Item> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);
    
    List<Item> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    List<Item> findByBranchAndIsActiveTrue(Branch branch);
    
    @Query("SELECT i FROM Item i WHERE i.isActive = true ORDER BY i.createdAt DESC")
    List<Item> findNewItems(int limit);
    
    @Query("SELECT i FROM Item i WHERE i.isActive = true ORDER BY (SELECT COUNT(oi) FROM OrderItem oi WHERE oi.item = i) DESC")
    List<Item> findTopSellingItems(int limit);
}
