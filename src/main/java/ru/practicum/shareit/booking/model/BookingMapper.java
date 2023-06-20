package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.dto.BookingDto;

import static ru.practicum.shareit.item.model.ItemMapper.mapItemToItemDto;
import static ru.practicum.shareit.user.model.UserMapper.mapUserEntityToUserDto;

public class BookingMapper {

    public static BookingDto mapBookingEntityToBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .item(mapItemToItemDto(booking.getItem()))
                .start(booking.getStartTime())
                .end(booking.getEndTime())
                .status(booking.getStatus())
                .booker(mapUserEntityToUserDto(booking.getUser()))
                .build();
    }

    public static Booking mapBookingDtoToBookingEntity(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .startTime(bookingDto.getStart())
                .endTime(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }
}
