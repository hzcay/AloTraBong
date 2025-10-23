package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Category;
import com.example.Alotrabong.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, String> {

    Page<Item> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    List<Item> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    Page<Item> findByIsActiveTrue(Pageable pageable);

    @Query("""
            select i from Item i
            where i.isActive = true
            order by i.createdAt desc
            """)
    Page<Item> findActiveOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            select i from Item i
            where i.isActive = true
            order by (select count(oi) from OrderItem oi where oi.item = i) desc
            """)
    Page<Item> findActiveOrderBySalesDesc(Pageable pageable);

    @Query("""
            select i from Item i
            where i.isActive = true and i.category.categoryId = :categoryId
            """)
    Page<Item> findActiveByCategoryId(@Param("categoryId") String categoryId, Pageable pageable);
}
