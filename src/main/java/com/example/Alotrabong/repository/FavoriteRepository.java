package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUser_UserId(Integer userId);
    Optional<Favorite> findByUser_UserIdAndItem_ItemId(Integer userId, Integer itemId);
    void deleteByUser_UserIdAndItem_ItemId(Integer userId, Integer itemId);
}
