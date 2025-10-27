package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ItemMediaDTO;
import java.util.List;

public interface ItemMediaService {
    ItemMediaDTO createMedia(ItemMediaDTO mediaDTO);
    ItemMediaDTO updateMedia(Integer mediaId, ItemMediaDTO mediaDTO);
    void deleteMedia(Integer mediaId);
    List<ItemMediaDTO> getMediaByItem(String itemId);
    void deleteAllMediaByItem(String itemId);
    ItemMediaDTO getMediaById(Integer mediaId);
}

