package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, Integer> {
}

