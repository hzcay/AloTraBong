package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.ItemOptionDTO;
import com.example.Alotrabong.dto.ItemOptionValueDTO;
import com.example.Alotrabong.entity.Item;
import com.example.Alotrabong.entity.ItemOption;
import com.example.Alotrabong.entity.ItemOptionValue;
import com.example.Alotrabong.repository.ItemOptionRepository;
import com.example.Alotrabong.repository.ItemOptionValueRepository;
import com.example.Alotrabong.repository.ItemRepository;
import com.example.Alotrabong.service.ItemOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemOptionServiceImpl implements ItemOptionService {

    private final ItemOptionRepository itemOptionRepository;
    private final ItemOptionValueRepository itemOptionValueRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemOptionDTO createOption(ItemOptionDTO optionDTO) {
        Item item = itemRepository.findById(optionDTO.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        ItemOption option = ItemOption.builder()
                .item(item)
                .optionName(optionDTO.getOptionName())
                .isRequired(optionDTO.getIsRequired() != null ? optionDTO.getIsRequired() : false)
                .build();

        ItemOption saved = itemOptionRepository.save(option);

        // Save values if provided
        if (optionDTO.getValues() != null && !optionDTO.getValues().isEmpty()) {
            for (ItemOptionValueDTO valueDTO : optionDTO.getValues()) {
                ItemOptionValue value = ItemOptionValue.builder()
                        .option(saved)
                        .valueName(valueDTO.getValueName())
                        .extraPrice(valueDTO.getExtraPrice())
                        .build();
                itemOptionValueRepository.save(value);
            }
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ItemOptionDTO updateOption(Integer optionId, ItemOptionDTO optionDTO) {
        ItemOption option = itemOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option not found"));

        option.setOptionName(optionDTO.getOptionName());
        if (optionDTO.getIsRequired() != null) {
            option.setIsRequired(optionDTO.getIsRequired());
        }

        ItemOption updated = itemOptionRepository.save(option);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteOption(Integer optionId) {
        // Delete all values first
        deleteAllValuesByOption(optionId);
        // Then delete the option
        itemOptionRepository.deleteById(optionId);
    }

    @Override
    public ItemOptionDTO getOptionById(Integer optionId) {
        ItemOption option = itemOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("Option not found"));
        return mapToDTO(option);
    }

    @Override
    public List<ItemOptionDTO> getOptionsByItem(String itemId) {
        return itemOptionRepository.findByItem_ItemId(itemId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllOptionsByItem(String itemId) {
        List<ItemOption> options = itemOptionRepository.findByItem_ItemId(itemId);
        for (ItemOption option : options) {
            deleteAllValuesByOption(option.getOptionId());
        }
        itemOptionRepository.deleteByItem_ItemId(itemId);
    }

    @Override
    @Transactional
    public ItemOptionValueDTO createOptionValue(ItemOptionValueDTO valueDTO) {
        ItemOption option = itemOptionRepository.findById(valueDTO.getOptionId())
                .orElseThrow(() -> new RuntimeException("Option not found"));

        ItemOptionValue value = ItemOptionValue.builder()
                .option(option)
                .valueName(valueDTO.getValueName())
                .extraPrice(valueDTO.getExtraPrice())
                .build();

        ItemOptionValue saved = itemOptionValueRepository.save(value);
        return mapValueToDTO(saved);
    }

    @Override
    @Transactional
    public ItemOptionValueDTO updateOptionValue(Integer valueId, ItemOptionValueDTO valueDTO) {
        ItemOptionValue value = itemOptionValueRepository.findById(valueId)
                .orElseThrow(() -> new RuntimeException("Option value not found"));

        value.setValueName(valueDTO.getValueName());
        if (valueDTO.getExtraPrice() != null) {
            value.setExtraPrice(valueDTO.getExtraPrice());
        }

        ItemOptionValue updated = itemOptionValueRepository.save(value);
        return mapValueToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteOptionValue(Integer valueId) {
        itemOptionValueRepository.deleteById(valueId);
    }

    @Override
    public ItemOptionValueDTO getOptionValueById(Integer valueId) {
        ItemOptionValue value = itemOptionValueRepository.findById(valueId)
                .orElseThrow(() -> new RuntimeException("Option value not found"));
        return mapValueToDTO(value);
    }

    @Override
    public List<ItemOptionValueDTO> getValuesByOption(Integer optionId) {
        return itemOptionValueRepository.findByOption_OptionId(optionId)
                .stream()
                .map(this::mapValueToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAllValuesByOption(Integer optionId) {
        itemOptionValueRepository.deleteByOption_OptionId(optionId);
    }

    private ItemOptionDTO mapToDTO(ItemOption option) {
        List<ItemOptionValueDTO> values = itemOptionValueRepository.findByOption_OptionId(option.getOptionId())
                .stream()
                .map(this::mapValueToDTO)
                .collect(Collectors.toList());

        return ItemOptionDTO.builder()
                .optionId(option.getOptionId())
                .itemId(option.getItem().getItemId())
                .optionName(option.getOptionName())
                .isRequired(option.getIsRequired())
                .values(values)
                .build();
    }

    private ItemOptionValueDTO mapValueToDTO(ItemOptionValue value) {
        return ItemOptionValueDTO.builder()
                .valueId(value.getValueId())
                .optionId(value.getOption().getOptionId())
                .valueName(value.getValueName())
                .extraPrice(value.getExtraPrice())
                .build();
    }
}


