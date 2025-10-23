package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Inventory;
import com.example.Alotrabong.entity.Item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBranch_BranchIdAndItem_ItemId(String branchId, Item itemId);
}
