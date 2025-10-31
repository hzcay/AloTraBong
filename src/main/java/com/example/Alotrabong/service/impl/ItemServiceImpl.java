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
                Page<Item> page = itemRepository.findTopSellingItems(PageRequest.of(0, limit));
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

        @Override
        @Transactional(readOnly = true)
        public List<ItemDTO> getTopFavoritedItems(int limit) {
                Page<Item> page = itemRepository.findTopFavoritedItems(
                                PageRequest.of(0, Math.max(1, limit)));
                 System.out.println("=== DEBUG TOP FAV ===");
                page.getContent().forEach(i -> System.out.println(i.getItemId() + " | " + i.getName()));
                return page.getContent().stream()
                                .map(this::convertToDTO)
                                .collect(Collectors.toList());
        }

        // ======================= helper =======================
        private ItemDTO convertToDTO(Item item) {
                return ItemDTO.builder()
                                .itemId(item.getItemId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .price(item.getPrice())
                                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null)
                                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                                .isActive(item.getIsActive())
                                .createdAt(item.getCreatedAt())
                                .updatedAt(item.getUpdatedAt())
                                .build();
        }
}
