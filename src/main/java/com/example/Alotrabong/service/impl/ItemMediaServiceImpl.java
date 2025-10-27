package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ItemMediaDTO;
import com.example.Alotrabong.entity.Item;
import com.example.Alotrabong.entity.ItemMedia;
import com.example.Alotrabong.repository.ItemMediaRepository;
import com.example.Alotrabong.repository.ItemRepository;
import com.example.Alotrabong.service.ItemMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemMediaServiceImpl implements ItemMediaService {

    private final ItemMediaRepository itemMediaRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemMediaDTO createMedia(ItemMediaDTO mediaDTO) {
        Item item = itemRepository.findById(mediaDTO.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        ItemMedia media = ItemMedia.builder()
                .item(item)
                .mediaUrl(mediaDTO.getMediaUrl())
                .sortOrder(mediaDTO.getSortOrder() != null ? mediaDTO.getSortOrder() : 0)
                .build();

        if (mediaDTO.getMediaType() != null) {
            try {
                media.setMediaType(Enum.valueOf(
                        com.example.Alotrabong.entity.MediaType.class,
                        mediaDTO.getMediaType().toUpperCase()
                ));
            } catch (IllegalArgumentException e) {
                media.setMediaType(com.example.Alotrabong.entity.MediaType.IMAGE);
            }
        }

        ItemMedia saved = itemMediaRepository.save(media);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ItemMediaDTO updateMedia(Integer mediaId, ItemMediaDTO mediaDTO) {
        ItemMedia media = itemMediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        media.setMediaUrl(mediaDTO.getMediaUrl());
        if (mediaDTO.getSortOrder() != null) {
            media.setSortOrder(mediaDTO.getSortOrder());
        }
        if (mediaDTO.getMediaType() != null) {
            try {
                media.setMediaType(Enum.valueOf(
                        com.example.Alotrabong.entity.MediaType.class,
                        mediaDTO.getMediaType().toUpperCase()
                ));
            } catch (IllegalArgumentException e) {
                // Keep existing type
            }
        }

        ItemMedia updated = itemMediaRepository.save(media);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteMedia(Integer mediaId) {
        itemMediaRepository.deleteById(mediaId);
    }

    @Override
    public ItemMediaDTO getMediaById(Integer mediaId) {
        ItemMedia media = itemMediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        return mapToDTO(media);
    }

    @Override
    public List<ItemMediaDTO> getMediaByItem(String itemId) {
        return itemMediaRepository.findByItem_ItemIdOrderBySortOrder(itemId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllMediaByItem(String itemId) {
        itemMediaRepository.deleteByItem_ItemId(itemId);
    }

    private ItemMediaDTO mapToDTO(ItemMedia media) {
        return ItemMediaDTO.builder()
                .mediaId(media.getMediaId())
                .itemId(media.getItem().getItemId())
                .mediaUrl(media.getMediaUrl())
                .mediaType(media.getMediaType() != null ? media.getMediaType().toString() : "IMAGE")
                .sortOrder(media.getSortOrder())
                .build();
    }
}