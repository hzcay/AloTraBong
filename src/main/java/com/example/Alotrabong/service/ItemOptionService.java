package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.ItemOptionDTO;
import com.example.Alotrabong.dto.ItemOptionValueDTO;
import java.util.List;

public interface ItemOptionService {
    ItemOptionDTO createOption(ItemOptionDTO optionDTO);
    ItemOptionDTO updateOption(Integer optionId, ItemOptionDTO optionDTO);
    void deleteOption(Integer optionId);
    ItemOptionDTO getOptionById(Integer optionId);
    List<ItemOptionDTO> getOptionsByItem(String itemId);
    void deleteAllOptionsByItem(String itemId);

    // Option Values
    ItemOptionValueDTO createOptionValue(ItemOptionValueDTO valueDTO);
    ItemOptionValueDTO updateOptionValue(Integer valueId, ItemOptionValueDTO valueDTO);
    void deleteOptionValue(Integer valueId);
    ItemOptionValueDTO getOptionValueById(Integer valueId);
    List<ItemOptionValueDTO> getValuesByOption(Integer optionId);
    void deleteAllValuesByOption(Integer optionId);
}

