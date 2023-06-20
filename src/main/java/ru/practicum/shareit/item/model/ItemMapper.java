package ru.practicum.shareit.item.model;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.function.Function;

public class ItemMapper {

    private static final Function<ItemRequest, Long> itemRequestLongFunction = itemRequest -> {
        if (itemRequest != null) {
            return itemRequest.getId();
        } else {
            return null;
        }
    };

    public static Item mapItemDtoToItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static Item mapItemDtoToItem(ItemDtoUpdate itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static CommentDto mapCommentEntityToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser().getName())
                .created(comment.getCreated())
                .build();
    }

    public static ItemDto mapItemToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(itemRequestLongFunction.apply(item.getItemRequest()))
                .build();
    }

    public static ItemBooking mapBookingDtoToItemBooking(Booking booking) {
        return ItemBooking.builder()
                .id(booking.getId())
                .bookerId(booking.getUser().getId())
                .build();
    }
}
