package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ItemOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOptionValueRepository extends JpaRepository<ItemOptionValue, Integer> {
    List<ItemOptionValue> findByOption_OptionId(Integer optionId);
    void deleteByOption_OptionId(Integer optionId);
}

