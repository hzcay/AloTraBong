package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.PromotionManagementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminPromotionService {

    // Promotion Management
    Page<PromotionManagementDTO> getAllPromotions(Pageable pageable, String search);

    PromotionManagementDTO getPromotionById(String promotionId);

    PromotionManagementDTO createPromotion(PromotionManagementDTO dto);

    PromotionManagementDTO updatePromotion(String promotionId, PromotionManagementDTO dto);

    void deletePromotion(String promotionId);

    void activatePromotion(String promotionId);

    void deactivatePromotion(String promotionId);

    long getTotalPromotionsCount();

    long getActivePromotionsCount();
}

