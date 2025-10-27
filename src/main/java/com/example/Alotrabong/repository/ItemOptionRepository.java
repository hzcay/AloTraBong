package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOptionRepository extends JpaRepository<ItemOption, Integer> {
    List<ItemOption> findByItem_ItemId(String itemId);
    void deleteByItem_ItemId(String itemId);
}

