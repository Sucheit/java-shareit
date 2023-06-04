package ru.practicum.shareit.item.model;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

public class ItemMapper {

    public static ItemEntity mapItemDtoToItemEntity(ItemDto itemDto) {
        return ItemEntity.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemEntity mapItemDtoToItemEntity(ItemDtoUpdate itemDto) {
        return ItemEntity.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemDto mapItemEntityToItemDto(ItemEntity itemEntity) {
        return ItemDto.builder()
                .id(itemEntity.getId())
                .name(itemEntity.getName())
                .description(itemEntity.getDescription())
                .available(itemEntity.getAvailable())
                .build();
    }

    public static CommentDto mapCommentEntityToCommentDto(CommentEntity commentEntity) {
        return CommentDto.builder()
                .id(commentEntity.getId())
                .text(commentEntity.getText())
                .authorName(commentEntity.getUserEntity().getName())
                .created(commentEntity.getCreated())
                .build();
    }

    public static ItemBooking mapBookingDtoToItemBooking(BookingDto bookingDto) {
        return ItemBooking.builder()
                .id(bookingDto.getId())
                .bookerId(bookingDto.getBooker().getId())
                .build();
    }
}
