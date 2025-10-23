package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Favorite;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser_UserId(String userId);

    Optional<Favorite> findByUser_UserIdAndItem_ItemId(String userId, String  itemId);

    @Modifying
    @Transactional
    long deleteByUser_UserIdAndItem_ItemId(String userId, String  itemId);
}
