package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Promotion;

import java.util.List;

public interface PromotionService {
    
    List<Promotion> getActivePromotions();
    
    List<Promotion> getPromotionsByBranch(String branchId);
    
    Promotion createPromotion(Promotion promotion);
    
    Promotion updatePromotion(String promotionId, Promotion promotion);
    
    void deletePromotion(String promotionId);
}