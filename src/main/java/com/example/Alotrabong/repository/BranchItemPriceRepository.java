package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.BranchItemPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Alotrabong.entity.Item;
import com.example.Alotrabong.entity.Branch;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchItemPriceRepository extends JpaRepository<BranchItemPrice, Integer> {
    
    List<BranchItemPrice> findByBranch_BranchId(String branchId);
    Optional<BranchItemPrice> findByItemAndBranch(Item item, Branch branch);
    Optional<BranchItemPrice> findByBranch_BranchIdAndItem_ItemId(String branchId, String itemId);
    
    @Query("SELECT bip FROM BranchItemPrice bip WHERE bip.branch.branchId = :branchId AND bip.isAvailable = true")
    List<BranchItemPrice> findAvailableByBranchId(@Param("branchId") String branchId);
    
    List<BranchItemPrice> findByBranchAndItem_IsActiveTrue(Branch branch);
}