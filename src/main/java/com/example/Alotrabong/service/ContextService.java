package com.example.Alotrabong.service;

import com.example.Alotrabong.entity.Category;
import com.example.Alotrabong.entity.Item;
import com.example.Alotrabong.entity.Promotion;
import com.example.Alotrabong.repository.CategoryRepository;
import com.example.Alotrabong.repository.ItemRepository;
import com.example.Alotrabong.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContextService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Lấy toàn bộ context public (menu, categories, promotions) để AI có thể trả lời câu hỏi
     */
    public String getPublicContext() {
        StringBuilder context = new StringBuilder();
        
        // 1. Categories
        List<Category> categories = categoryRepository.findAll()
                .stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .toList();
        context.append("=== DANH MỤC MÓN ĂN ===\n");
        for (Category cat : categories) {
            context.append(String.format("- %s: %s\n", cat.getName(), 
                    cat.getDescription() != null ? cat.getDescription() : ""));
        }
        
        // 2. Menu items (top 50 để không quá dài)
        List<Item> items = itemRepository.findByIsActive(true)
                .stream()
                .limit(50)
                .toList();
        context.append("\n=== THỰC ĐƠN HIỆN CÓ ===\n");
        for (Item item : items) {
            context.append(String.format("- %s: %s - Giá: %,.0f VND\n", 
                    item.getName(),
                    item.getDescription() != null ? item.getDescription() : "",
                    item.getPrice().doubleValue()));
        }
        
        // 3. Active promotions
        List<Promotion> promotions = promotionRepository.findByIsActiveTrue();
        if (!promotions.isEmpty()) {
            context.append("\n=== KHUYẾN MÃI ĐANG ÁP DỤNG ===\n");
            for (Promotion promo : promotions) {
                context.append(String.format("- %s: %s - Giảm %,.0f VND\n",
                        promo.getName(),
                        promo.getDescription() != null ? promo.getDescription() : "",
                        promo.getDiscountValue() != null ? promo.getDiscountValue().doubleValue() : 0));
            }
        }
        
        return context.toString();
    }

    /**
     * Tìm kiếm items theo keyword
     */
    public List<Item> searchItems(String keyword) {
        return itemRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
    }

    /**
     * Lấy thông tin chi tiết của một item
     */
    public String getItemDetails(Item item) {
        return String.format("Tên: %s, Mô tả: %s, Giá: %,.0f VND, Danh mục: %s",
                item.getName(),
                item.getDescription() != null ? item.getDescription() : "Không có mô tả",
                item.getPrice().doubleValue(),
                item.getCategory() != null ? item.getCategory().getName() : "Không phân loại");
    }

    /**
     * Lấy context compact cho AI (chỉ tổng quan, không chi tiết)
     */
    public String getCompactContext() {
        long categoryCount = categoryRepository.count();
        long itemCount = itemRepository.findByIsActive(true).size();
        long promotionCount = promotionRepository.countByIsActive(true);
        
        return String.format("""
                === THÔNG TIN CƠ BẢN VỀ THỰC ĐƠN ===
                - Số danh mục: %d
                - Số món ăn: %d
                - Số chương trình khuyến mãi: %d
                
                Món ăn được chia thành các danh mục: Cơm, Mì, Tráng miệng, Đồ uống
                Nhà hàng cung cấp dịch vụ giao hàng tận nơi với thanh toán COD hoặc online.
                """, categoryCount, itemCount, promotionCount);
    }
}

