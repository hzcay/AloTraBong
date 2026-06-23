package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ItemMedia;
import com.example.Alotrabong.entity.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemMediaRepository extends JpaRepository<ItemMedia, Integer> {
    List<ItemMedia> findByItem_ItemIdOrderBySortOrder(String itemId);

    void deleteByItem_ItemId(String itemId);

    List<ItemMedia> findByItem_ItemIdOrderBySortOrderAsc(String itemId);

    List<ItemMedia> findByItem_ItemIdAndMediaTypeOrderBySortOrderAsc(String itemId, MediaType mediaType);

    Optional<ItemMedia> findFirstByItem_ItemIdAndMediaTypeOrderBySortOrderAscMediaIdAsc(
            String itemId, MediaType mediaType);

    List<ItemMedia> findByItem_ItemIdAndMediaTypeOrderBySortOrderAscMediaIdAsc(
            String itemId, MediaType mediaType);
}
