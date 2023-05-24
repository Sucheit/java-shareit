package ru.practicum.shareit.item.model;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

public class ItemMapper {

    public static ItemEntity mapItemDtoToItemEntity(ItemDto itemDto) {
        return ItemEntity.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .userEntity(itemDto.getUserEntity())
                .build();
    }

    public static ItemEntity mapItemDtoToItemEntity(ItemDtoUpdate itemDto) {
        return ItemEntity.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .userEntity(itemDto.getUserEntity())
                .build();
    }

    public static ItemDto mapItemEntityToItemDto(ItemEntity itemEntity) {
        return ItemDto.builder()
                .id(itemEntity.getId())
                .name(itemEntity.getName())
                .description(itemEntity.getDescription())
                .available(itemEntity.getAvailable())
                .userEntity(itemEntity.getUserEntity())
                .build();
    }
}
