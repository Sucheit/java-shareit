package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @Valid @RequestBody BookingDto bookingDto) {
        BookingDto booking = bookingService.addBooking(userId, bookingDto);
        log.info("Добавили бронирование: {}", booking);
        return booking;
    }

    @PatchMapping(value = "/{bookingId}")
    public BookingDto updateBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestParam(value = "approved") boolean approved,
                                    @PathVariable long bookingId) {
        BookingDto updatedBooking = bookingService.updateBooking(userId, approved, bookingId);
        log.info("Обновили бронирование: {}", updatedBooking);
        return updatedBooking;
    }

    @GetMapping(value = "/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @PathVariable long bookingId) {
        BookingDto bookingDto = bookingService.getBooking(userId, bookingId);
        log.info("Получили бронирование: {}", bookingDto);
        return bookingDto;
    }

    @GetMapping
    public List<BookingDto> getBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state) {
        List<BookingDto> bookings = bookingService.getBookingsByBookerId(userId, state);
        log.info("Получили список бронирований пользователя id={}, длина={}", userId, bookings.size());
        return bookings;
    }

    @GetMapping(value = "/owner")
    public List<BookingDto> getBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state) {
        List<BookingDto> bookings = bookingService.getBookingsByOwnerId(userId, state);
        log.info("Получили список бронирований владельца вещей id={}, длина={}", userId, bookings.size());
        return bookings;
    }

}
