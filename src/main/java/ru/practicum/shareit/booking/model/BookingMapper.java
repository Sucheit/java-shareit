package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.dto.BookingDto;

public class BookingMapper {

    public static BookingDto mapBookingEntityToBookingDto(BookingEntity bookingEntity) {
        return BookingDto.builder()
                .id(bookingEntity.getId())
                .item(bookingEntity.getItemEntity())
                .start(bookingEntity.getStartTime())
                .end(bookingEntity.getEndTime())
                .status(bookingEntity.getStatus())
                .booker(bookingEntity.getUserEntity())
                .build();
    }

    public static BookingEntity mapBookingDtoToBookingEntity(BookingDto bookingDto) {
        return BookingEntity.builder()
                .id(bookingDto.getId())
                .itemEntity(bookingDto.getItem())
                .startTime(bookingDto.getStart())
                .endTime(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .userEntity(bookingDto.getBooker())
                .build();
    }
}
