package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.entity.Promotion;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.PromotionRepository;
import com.example.Alotrabong.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promotion> getPromotionsByBranch(String branchId) {
        return promotionRepository.findByBranchIdAndIsActiveTrue(branchId);
    }

    @Override
    public Promotion createPromotion(Promotion promotion) {
        log.info("Creating promotion: {}", promotion.getName());
        Promotion savedPromotion = promotionRepository.save(promotion);
        log.info("Promotion created successfully: {}", savedPromotion.getPromotionId());
        return savedPromotion;
    }

    @Override
    public Promotion updatePromotion(String promotionId, Promotion promotion) {
        log.info("Updating promotion: {}", promotionId);
        Promotion existingPromotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        
        existingPromotion.setName(promotion.getName());
        existingPromotion.setDescription(promotion.getDescription());
        existingPromotion.setDiscountType(promotion.getDiscountType());
        existingPromotion.setDiscountValue(promotion.getDiscountValue());
        existingPromotion.setStartDate(promotion.getStartDate());
        existingPromotion.setEndDate(promotion.getEndDate());
        
        existingPromotion = promotionRepository.save(existingPromotion);
        log.info("Promotion updated successfully: {}", promotionId);
        return existingPromotion;
    }

    @Override
    public void deletePromotion(String promotionId) {
        log.info("Deleting promotion: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
        
        log.info("Promotion deactivated: {}", promotionId);
    }
}
