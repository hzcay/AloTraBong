package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    List<Review> findByItemIdOrderByCreatedAtDesc(String itemId);
    
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);
}