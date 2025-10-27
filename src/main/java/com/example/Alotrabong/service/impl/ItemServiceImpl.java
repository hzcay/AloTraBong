package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ItemDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {

        private final ItemRepository itemRepository;
        private final CategoryRepository categoryRepository;
        private final BranchRepository branchRepository;
        private final BranchItemPriceRepository branchItemPriceRepository;

        // ====== NEW: bơm thêm 2 repo để tính best-seller & favorites ======
        private final OrderItemRepository orderItemRepository; // add repo này
        private final FavoriteRepository favoriteRepository; // add repo này

        @Override
        public ItemDTO createItem(ItemDTO itemDTO) {
                log.info("Creating new item: {}", itemDTO.getName());

                Category category = categoryRepository.findById(itemDTO.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                Item item = Item.builder()
                                .name(itemDTO.getName())
                                .description(itemDTO.getDescription())
                                .price(itemDTO.getPrice())
                                .category(category)
                                .isActive(true)
                                .build();

                item = itemRepository.save(item);
                log.info("Item created successfully: {}", item.getItemId());

                return convertToDTO(item);
        }

        @Override
        @Transactional(readOnly = true)
        public ItemDTO getItemById(String itemId) {
                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                return convertToDTO(item);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> getAllItems() {
                return itemRepository.findAll().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ItemDTO> getItemsByCategory(String categoryId, Pageable pageable) {
                Category category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                return itemRepository.findByCategoryAndIsActiveTrue(category, pageable)
                                .map(this::convertToDTO);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> getItemsByBranch(String branchId) {
                // Item không có field branch → lấy qua bảng giá theo chi nhánh
                Branch branch = branchRepository.findById(branchId)
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

                return branchItemPriceRepository.findByBranchAndItem_IsActiveTrue(branch).stream()
                                .map(BranchItemPrice::getItem)
                                .distinct()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> searchItems(String keyword) {
                // Repo hiện trả về List (không Pageable)
                return itemRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword).stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> getTopSellingItems(int limit) {
                // Dùng repo: findActiveOrderBySalesDesc(Pageable)
                Page<Item> page = itemRepository.findActiveOrderBySalesDesc(
                                PageRequest.of(0, Math.max(1, limit)));
                return page.getContent().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> getNewItems(int limit) {
                // Dùng repo: findActiveOrderByCreatedAtDesc(Pageable)
                Page<Item> page = itemRepository.findActiveOrderByCreatedAtDesc(
                                PageRequest.of(0, Math.max(1, limit)));
                return page.getContent().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        @Override
        public ItemDTO updateItem(String itemId, ItemDTO itemDTO) {
                log.info("Updating item: {}", itemId);

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

                Category category = categoryRepository.findById(itemDTO.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

                item.setName(itemDTO.getName());
                item.setDescription(itemDTO.getDescription());
                item.setPrice(itemDTO.getPrice());
                item.setCategory(category);

                item = itemRepository.save(item);
                log.info("Item updated successfully: {}", itemId);

                return convertToDTO(item);
        }

        @Override
        public void deleteItem(String itemId) {
                log.info("Deleting item: {}", itemId);

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

                item.setIsActive(false);
                itemRepository.save(item);

                log.info("Item deactivated: {}", itemId);
        }

        @Override
        public ItemDTO activateItem(String itemId) {
                log.info("Activating item: {}", itemId);

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

                item.setIsActive(true);
                item = itemRepository.save(item);

                log.info("Item activated: {}", itemId);
                return convertToDTO(item);
        }

        @Override
        public ItemDTO setBranchPrice(String itemId, String branchId, BigDecimal price) {
                log.info("Setting price for item: {} in branch: {} to {}", itemId, branchId, price);

                Item item = itemRepository.findById(itemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

                Branch branch = branchRepository.findById(branchId)
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

                BranchItemPrice branchPrice = branchItemPriceRepository.findByItemAndBranch(item, branch)
                                .orElse(BranchItemPrice.builder()
                                                .item(item)
                                                .branch(branch)
                                                .price(price)
                                                .build());

                branchPrice.setPrice(price);
                branchItemPriceRepository.save(branchPrice);

                log.info("Branch price set successfully");
                return convertToDTO(item);
        }

        @Transactional(readOnly = true)
        public List<ItemDTO> getTopSellingItemsSince(int days, int limit) {
                LocalDateTime since = LocalDateTime.now().minusDays(Math.max(1, days));

                // chỉ tính các order đã chốt/đang xử lý/đã giao
                List<OrderStatus> okStatuses = List.of(
                                OrderStatus.CONFIRMED,
                                OrderStatus.PREPARING,
                                OrderStatus.READY,
                                OrderStatus.DELIVERING,
                                OrderStatus.DELIVERED);

                Pageable pageable = PageRequest.of(0, Math.max(1, limit));
                List<Object[]> rows = orderItemRepository.findTopSellingSince(since, okStatuses, pageable);

                List<String> ids = rows.stream().map(r -> (String) r[0]).toList();
                if (ids.isEmpty())
                        return List.of();

                Map<String, Item> byId = itemRepository.findAllById(ids).stream()
                                .collect(Collectors.toMap(Item::getItemId, it -> it));

                return ids.stream()
                                .map(byId::get)
                                .filter(Objects::nonNull)
                                .map(this::convertToDTO)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ItemDTO> getTopFavoritedItems(int limit) {
                List<Object[]> rows = favoriteRepository.findTopFavorited(PageRequest.of(0, Math.max(1, limit)));
                List<String> ids = rows.stream().map(r -> (String) r[0]).toList();
                if (ids.isEmpty())
                        return List.of();

                Map<String, Item> byId = itemRepository.findAllById(ids).stream()
                                .collect(Collectors.toMap(Item::getItemId, it -> it));

                return ids.stream()
                                .map(byId::get)
                                .filter(Objects::nonNull)
                                .map(this::convertToDTO)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ItemDTO> getUserFavoriteItems(String userId, int limit) {
                return favoriteRepository
                                .findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Math.max(1, limit)))
                                .stream()
                                .map(Favorite::getItem)
                                .map(this::convertToDTO)
                                .toList();
        }

        // ======================= helper =======================
        private ItemDTO convertToDTO(Item item) {
                return ItemDTO.builder()
                                .itemId(item.getItemId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .price(item.getPrice())
                                .categoryId(item.getCategory().getCategoryId())
                                .categoryName(item.getCategory().getName())
                                .isActive(item.getIsActive())
                                .createdAt(item.getCreatedAt())
                                .updatedAt(item.getUpdatedAt())
                                .build();
        }
}
