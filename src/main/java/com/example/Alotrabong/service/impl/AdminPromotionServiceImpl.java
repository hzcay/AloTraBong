package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.PromotionManagementDTO;
import com.example.Alotrabong.entity.Promotion;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.PromotionRepository;
import com.example.Alotrabong.service.AdminPromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminPromotionServiceImpl implements AdminPromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public Page<PromotionManagementDTO> getAllPromotions(Pageable pageable, String search) {
        log.info("Fetching all promotions with search: {}", search);
        Page<Promotion> promotions = promotionRepository.findAll(pageable);
        return promotions.map(this::convertToDTO);
    }

    @Override
    public PromotionManagementDTO getPromotionById(String promotionId) {
        log.info("Fetching promotion by id: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        return convertToDTO(promotion);
    }

    @Override
    public PromotionManagementDTO createPromotion(PromotionManagementDTO dto) {
        log.info("Creating new promotion: {}", dto.getCode());
        Promotion promotion = Promotion.builder()
                .name(dto.getCode())
                .description(dto.getDescription())
                .discountType(dto.getType())
                .discountValue(dto.getValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(true)
                .build();

        promotion = promotionRepository.save(promotion);
        return convertToDTO(promotion);
    }

    @Override
    public PromotionManagementDTO updatePromotion(String promotionId, PromotionManagementDTO dto) {
        log.info("Updating promotion: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        promotion.setName(dto.getCode());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountType(dto.getType());
        promotion.setDiscountValue(dto.getValue());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());

        promotion = promotionRepository.save(promotion);
        return convertToDTO(promotion);
    }

    @Override
    public void deletePromotion(String promotionId) {
        log.info("Deleting promotion: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        promotionRepository.delete(promotion);
    }

    @Override
    public void activatePromotion(String promotionId) {
        log.info("Activating promotion: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        promotion.setIsActive(true);
        promotionRepository.save(promotion);
    }

    @Override
    public void deactivatePromotion(String promotionId) {
        log.info("Deactivating promotion: {}", promotionId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }

    @Override
    public long getTotalPromotionsCount() {
        return promotionRepository.count();
    }

    @Override
    public long getActivePromotionsCount() {
        return promotionRepository.countByIsActive(true);
    }

    private PromotionManagementDTO convertToDTO(Promotion promotion) {
        return PromotionManagementDTO.builder()
                .promotionId(promotion.getPromotionId())
                .code(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getDiscountType())
                .value(promotion.getDiscountValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .createdAt(promotion.getCreatedAt())
                .build();
    }
}

