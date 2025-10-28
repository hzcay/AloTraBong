package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    
    Optional<Inventory> findByBranch_BranchIdAndItem_ItemId(String branchId, String itemId);
    
    List<Inventory> findByBranch_BranchId(String branchId);
    
    @Query("SELECT i FROM Inventory i WHERE i.branch.branchId = :branchId AND i.quantity <= i.safetyStock")
    List<Inventory> findLowStockItems(@Param("branchId") String branchId);
    
    @Query("SELECT i FROM Inventory i WHERE i.branch.branchId = :branchId AND i.quantity = 0")
    List<Inventory> findOutOfStockItems(@Param("branchId") String branchId);
}