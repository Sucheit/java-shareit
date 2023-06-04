package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.dto.BookingDto;

import static ru.practicum.shareit.item.model.ItemMapper.mapItemEntityToItemDto;
import static ru.practicum.shareit.user.model.UserMapper.mapUserEntityToUserDto;

public class BookingMapper {

    public static BookingDto mapBookingEntityToBookingDto(BookingEntity bookingEntity) {
        return BookingDto.builder()
                .id(bookingEntity.getId())
                .item(mapItemEntityToItemDto(bookingEntity.getItemEntity()))
                .start(bookingEntity.getStartTime())
                .end(bookingEntity.getEndTime())
                .status(bookingEntity.getStatus())
                .booker(mapUserEntityToUserDto(bookingEntity.getUserEntity()))
                .build();
    }

    public static BookingEntity mapBookingDtoToBookingEntity(BookingDto bookingDto) {
        return BookingEntity.builder()
                .id(bookingDto.getId())
                .startTime(bookingDto.getStart())
                .endTime(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }
}
