package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Favorite;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser_UserId(String userId);

    Optional<Favorite> findByUser_UserIdAndItem_ItemId(String userId, String itemId);

    @Modifying
    @Transactional
    long deleteByUser_UserIdAndItem_ItemId(String userId, String itemId);

    @Query("""
              SELECT f.item.itemId, COUNT(f.favId)
              FROM Favorite f
              GROUP BY f.item.itemId
              ORDER BY COUNT(f.favId) DESC
            """)
    List<Object[]> findTopFavorited(Pageable pageable);

    List<Favorite> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
