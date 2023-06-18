package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingDtoToBookingEntity;
import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingEntityToBookingDto;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;


    @Transactional
    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден!", userId)));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена!",
                        bookingDto.getItemId())));
        if (userId.equals(item.getUser().getId())) {
            throw new NotFoundException("Хозяин вещи не может забронировать свою вещь!");
        }
        if (item.getAvailable().equals(Boolean.FALSE)) {
            throw new BadRequestException("Вещь не доступна!");
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Время начала и конца бронирования совпадают!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BadRequestException("Время конца бронирования раньше начала!");
        }
        bookingDto.setStatus(Status.WAITING);
        Booking booking = mapBookingDtoToBookingEntity(bookingDto);
        booking.setItem(item);
        booking.setUser(user);
        return mapBookingEntityToBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateBooking(Long userId, boolean approved, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", bookingId)));
        if (!userId.equals(booking.getItem().getUser().getId())) {
            throw new NotFoundException("Бронирование может редактировать только хозяин вещи!");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронирование уже рассмотрено хозяином вещи!");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking updateBooking = bookingRepository.save(booking);
        return mapBookingEntityToBookingDto(updateBooking);
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(Long userId, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", userId)));
        if (!userId.equals(booking.getUser().getId()) &&
                !userId.equals(booking.getItem().getUser().getId())) {
            throw new NotFoundException("Бронирование может запросить только хозяин вещи или бронирующий!");
        }
        return mapBookingEntityToBookingDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByBookerId(Long bookerId, String state) {
        if (userRepository.findById(bookerId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", bookerId));
        }
        State methodState;
        try {
            methodState = State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        Stream<Booking> bookingEntityStream;
        switch (methodState) {
            case ALL:
                bookingEntityStream = bookingRepository.findByUserIdOrderByStartTimeDesc(bookerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository
                        .findByUserIdAndStatusEqualsOrderByStartTimeDesc(bookerId, Status.WAITING).stream();
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository
                        .findByUserIdAndStatusEqualsOrderByStartTimeDesc(bookerId, Status.REJECTED).stream();
                break;
            case PAST:
                bookingEntityStream = bookingRepository
                        .findByUserIdAndEndTimeBeforeOrderByStartTimeDesc(bookerId, LocalDateTime.now()).stream();
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository
                        .findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(bookerId,
                                LocalDateTime.now(), LocalDateTime.now()).stream();
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository
                        .findByUserIdAndStartTimeAfterOrderByStartTimeDesc(bookerId, LocalDateTime.now()).stream();
                break;
            default:
                bookingEntityStream = Stream.empty();
        }
        return bookingEntityStream
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwnerId(Long ownerId, String state) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", ownerId));
        }
        State methodState;
        try {
            methodState = State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        Stream<Booking> bookingEntityStream;
        switch (methodState) {
            case ALL:
                bookingEntityStream = bookingRepository.findByItemUserIdOrderByStartTimeDesc(ownerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository
                        .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(ownerId, Status.WAITING).stream();
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository
                        .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(ownerId, Status.REJECTED).stream();
                break;
            case PAST:
                bookingEntityStream = bookingRepository
                        .findByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(ownerId, LocalDateTime.now()).stream();
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository
                        .findByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(ownerId,
                                LocalDateTime.now(), LocalDateTime.now()).stream();
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository
                        .findByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(ownerId, LocalDateTime.now()).stream();
                break;
            default:
                bookingEntityStream = Stream.empty();
        }
        return bookingEntityStream
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }
}
