package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ReviewDTO;
import com.example.Alotrabong.entity.Review;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.ReviewRepository;
import com.example.Alotrabong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByItem(String itemId) {
        return reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUser(String userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        log.info("Creating review for item: {} by user: {}", reviewDTO.getItemId(), reviewDTO.getUserId());
        
        Review review = Review.builder()
                .itemId(reviewDTO.getItemId())
                .userId(reviewDTO.getUserId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .isActive(true)
                .build();
        
        review = reviewRepository.save(review);
        log.info("Review created successfully: {}", review.getReviewId());
        
        return convertToDTO(review);
    }

    @Override
    public ReviewDTO updateReview(String reviewId, ReviewDTO reviewDTO) {
        log.info("Updating review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        review = reviewRepository.save(review);
        log.info("Review updated successfully: {}", reviewId);
        
        return convertToDTO(review);
    }

    @Override
    public void deleteReview(String reviewId) {
        log.info("Deleting review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setIsActive(false);
        reviewRepository.save(review);
        
        log.info("Review deactivated: {}", reviewId);
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .itemId(review.getItemId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .isActive(review.getIsActive())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
