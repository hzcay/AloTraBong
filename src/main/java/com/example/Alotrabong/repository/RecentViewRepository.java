package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.RecentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentView, Integer> {
    List<RecentView> findByUser_UserIdOrderByViewedAtDesc(String userId);
}
