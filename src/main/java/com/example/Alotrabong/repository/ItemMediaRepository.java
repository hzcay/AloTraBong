package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ItemMedia;
import io.swagger.v3.oas.models.media.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ItemMediaRepository extends JpaRepository<ItemMedia, Integer> {
    List<ItemMedia> findByItem_ItemIdOrderBySortOrder(String itemId);
    void deleteByItem_ItemId(String itemId);
    List<ItemMedia> findByItem_ItemIdOrderBySortOrderAsc(String itemId);
    List<ItemMedia> findByItem_ItemIdAndMediaTypeOrderBySortOrderAsc(String itemId, MediaType mediaType);
}
