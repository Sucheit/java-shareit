package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

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
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден!", userId)));
        ItemEntity itemEntity = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена!",
                        bookingDto.getItemId())));
        if (userId.equals(itemEntity.getUserEntity().getId())) {
            throw new NotFoundException("Хозяин вещи не может забронировать свою вещь!");
        }
        if (itemEntity.getAvailable().equals(Boolean.FALSE)) {
            throw new BadRequestException("Вещь не доступна!");
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Время начала и конца бронирования совпадают!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BadRequestException("Время конца бронирования раньше начала!");
        }
        bookingDto.setStatus(Status.WAITING);
        bookingDto.setBooker(userEntity);
        bookingDto.setItem(itemEntity);
        BookingEntity bookingEntity = bookingRepository.save(mapBookingDtoToBookingEntity(bookingDto));
        return mapBookingEntityToBookingDto(bookingEntity);
    }

    @Transactional
    public BookingDto updateBooking(Long userId, String approved, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        BookingEntity bookingEntity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", bookingId)));
        if (!userId.equals(bookingEntity.getItemEntity().getUserEntity().getId())) {
            throw new NotFoundException("Бронирование может редактировать только хозяин вещи!");
        }
        if (!bookingEntity.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронирование уже рассмотрено хозяином вещи!");
        }
        switch (approved) {
            case "true":
                bookingEntity.setStatus(Status.APPROVED);
                break;
            case "false":
                bookingEntity.setStatus(Status.REJECTED);
                break;
            default:
                throw new BadRequestException("Не верное значение параметра approved!");
        }
        BookingEntity updateBookingEntity = bookingRepository.save(bookingEntity);
        return mapBookingEntityToBookingDto(updateBookingEntity);
    }

    @Transactional
    public BookingDto getBooking(Long userId, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        BookingEntity bookingEntity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование id=%s не найдено!", userId)));
        if (!userId.equals(bookingEntity.getUserEntity().getId()) &&
                !userId.equals(bookingEntity.getItemEntity().getUserEntity().getId())) {
            throw new NotFoundException("Бронирование может запросить только хозяин вещи или бронирующий!");
        }
        return mapBookingEntityToBookingDto(bookingEntity);
    }

    @Transactional
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
        Stream<BookingEntity> bookingEntityStream;
        switch (methodState) {
            case ALL:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository
                        .findByUserEntityIdAndStatusEquals(bookerId, Status.WAITING).stream();
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository
                        .findByUserEntityIdAndStatusEquals(bookerId, Status.REJECTED).stream();
                break;
            case PAST:
                bookingEntityStream = bookingRepository
                        .findByUserEntityIdAndEndTimeBefore(bookerId, LocalDateTime.now()).stream();
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository
                        .findByUserEntityIdAndStartTimeBeforeAndEndTimeAfter(bookerId,
                                LocalDateTime.now(), LocalDateTime.now()).stream();
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository
                        .findByUserEntityIdAndStartTimeAfter(bookerId, LocalDateTime.now()).stream();
                break;
            default:
                bookingEntityStream = Stream.empty();
        }
        return bookingEntityStream
                .sorted()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional
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
        Stream<BookingEntity> bookingEntityStream;
        switch (methodState) {
            case ALL:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository
                        .findByItemEntityUserEntityIdAndStatusEquals(ownerId, Status.WAITING).stream();
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository
                        .findByItemEntityUserEntityIdAndStatusEquals(ownerId, Status.REJECTED).stream();
                break;
            case PAST:
                bookingEntityStream = bookingRepository
                        .findByItemEntityUserEntityIdAndEndTimeBefore(ownerId, LocalDateTime.now()).stream();
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository
                        .findByItemEntityUserEntityIdAndStartTimeBeforeAndEndTimeAfter(ownerId,
                                LocalDateTime.now(), LocalDateTime.now()).stream();
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository
                        .findByItemEntityUserEntityIdAndStartTimeAfter(ownerId, LocalDateTime.now()).stream();
                break;
            default:
                bookingEntityStream = Stream.empty();
        }
        return bookingEntityStream
                .sorted()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }
}
