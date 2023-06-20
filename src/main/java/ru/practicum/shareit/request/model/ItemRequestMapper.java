package ru.practicum.shareit.request.model;

import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper {

    public static ItemRequestDto mapItemRequestToDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .userId(itemRequest.getUser().getId())
                .created(itemRequest.getCreated())
                .build();
    }
}
