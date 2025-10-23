package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.BranchItemPrice;
import com.example.Alotrabong.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchItemPriceRepository extends JpaRepository<BranchItemPrice, Integer> {
    
    Optional<BranchItemPrice> findByItemAndBranch(Item item, Branch branch);

    List<BranchItemPrice> findByBranchAndItem_IsActiveTrue(Branch branch);
    
}
