package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    
    List<ReviewDTO> getReviewsByItem(String itemId);
    
    List<ReviewDTO> getReviewsByUser(String userId);
    
    ReviewDTO createReview(ReviewDTO reviewDTO);
    
    ReviewDTO updateReview(String reviewId, ReviewDTO reviewDTO);
    
    void deleteReview(String reviewId);
}