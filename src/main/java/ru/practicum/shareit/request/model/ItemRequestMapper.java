package ru.practicum.shareit.request.model;

import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestMapper {

    public static ItemRequest mapDtoToItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .build();
    }

    public static ItemRequestDto mapItemRequestToDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .userId(itemRequest.getUser().getId())
                .created(itemRequest.getCreated())
                .build();
    }
}
