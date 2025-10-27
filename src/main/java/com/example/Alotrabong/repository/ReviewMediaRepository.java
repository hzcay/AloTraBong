package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ReviewMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {
    List<ReviewMedia> findByReview_ReviewId(String reviewId);
}
