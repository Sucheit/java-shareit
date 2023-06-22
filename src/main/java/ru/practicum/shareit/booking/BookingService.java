package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingDtoToBookingEntity;
import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingEntityToBookingDto;
import static ru.practicum.shareit.exception.Validation.validatePagination;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingService {

    BookingRepository bookingRepository;

    UserRepository userRepository;

    ItemRepository itemRepository;

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

    private static State validateBookingState(String str) {
        State state;
        try {
            state = State.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", str));
        }
        return state;
    }

    @Transactional
    public BookingDto updateBooking(Long ownerId, boolean approved, Long bookingId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", ownerId));
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", bookingId)));
        if (!ownerId.equals(booking.getItem().getUser().getId())) {
            throw new NotFoundException("Бронирование может редактировать только хозяин вещи!");
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронирование уже рассмотрено хозяином вещи!");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updateBooking = bookingRepository.save(booking);
        return mapBookingEntityToBookingDto(updateBooking);
    }

    @Transactional(readOnly = true)
    public BookingDto getBooking(long userId, long bookingId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", userId)));
        if (userId != booking.getUser().getId() &&
                (userId != booking.getItem().getUser().getId())) {
            throw new NotFoundException("Бронирование может запросить только хозяин вещи или бронирующий!");
        }
        return mapBookingEntityToBookingDto(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByBookerId(long bookerId, String state, int from, int size) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", bookerId));
        }
        validatePagination(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        State methodState = validateBookingState(state);
        List<Booking> bookingList = Collections.emptyList();
        switch (methodState) {
            case ALL:
                bookingList = bookingRepository
                        .findByUserIdOrderByStartTimeDesc(bookerId, pageRequest);
                break;
            case WAITING:
                bookingList = bookingRepository
                        .findByUserIdAndStatusEqualsOrderByStartTimeDesc(bookerId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingList = bookingRepository
                        .findByUserIdAndStatusEqualsOrderByStartTimeDesc(bookerId, Status.REJECTED, pageRequest);
                break;
            case PAST:
                bookingList = bookingRepository
                        .findByUserIdAndEndTimeBeforeOrderByStartTimeDesc(bookerId, LocalDateTime.now(), pageRequest);
                break;
            case CURRENT:
                bookingList = bookingRepository
                        .findByUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(bookerId,
                                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookingList = bookingRepository
                        .findByUserIdAndStartTimeAfterOrderByStartTimeDesc(bookerId, LocalDateTime.now(), pageRequest);
                break;
        }
        return bookingList.stream()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwnerId(long ownerId, String state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", ownerId));
        }
        validatePagination(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        State methodState = validateBookingState(state);
        List<Booking> bookingList = Collections.emptyList();
        switch (methodState) {
            case ALL:
                bookingList = bookingRepository
                        .findByItemUserIdOrderByStartTimeDesc(ownerId, pageRequest);
                break;
            case WAITING:
                bookingList = bookingRepository
                        .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(ownerId, Status.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingList = bookingRepository
                        .findByItemUserIdAndStatusEqualsOrderByStartTimeDesc(ownerId, Status.REJECTED, pageRequest);
                break;
            case PAST:
                bookingList = bookingRepository
                        .findByItemUserIdAndEndTimeBeforeOrderByStartTimeDesc(ownerId, LocalDateTime.now(), pageRequest);
                break;
            case CURRENT:
                bookingList = bookingRepository
                        .findByItemUserIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(ownerId,
                                LocalDateTime.now(), LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookingList = bookingRepository
                        .findByItemUserIdAndStartTimeAfterOrderByStartTimeDesc(ownerId, LocalDateTime.now(), pageRequest);
                break;
        }
        return bookingList.stream()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }
}
