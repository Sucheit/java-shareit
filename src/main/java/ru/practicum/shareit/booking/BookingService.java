package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingDtoToBookingEntity;
import static ru.practicum.shareit.booking.model.BookingMapper.mapBookingEntityToBookingDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;


    public BookingDto addBooking(Long userId, BookingDto bookingDto) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Optional<ItemEntity> optionalItemEntity = itemRepository.findById(bookingDto.getItemId());
        if (optionalItemEntity.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id=%s не найдена!", bookingDto.getItemId()));
        }
        if (userId.equals(optionalItemEntity.get().getUserEntity().getId())) {
            throw new NotFoundException("Хозяин вещи не может забронировать свою вещь!");
        }
        if (optionalItemEntity.get().getAvailable().equals(Boolean.FALSE)) {
            throw new BadRequestException("Вещь не доступна!");
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Время начала и конца бронирования совпадают!");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BadRequestException("Время конца бронирования раньше начала!");
        }
        bookingDto.setStatus(Status.WAITING);
        bookingDto.setBooker(optionalUserEntity.get());
        bookingDto.setItem(optionalItemEntity.get());
        BookingEntity bookingEntity = bookingRepository.save(mapBookingDtoToBookingEntity(bookingDto));
        return mapBookingEntityToBookingDto(bookingEntity);
    }

    public BookingDto updateBooking(Long userId, String approved, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Optional<BookingEntity> optionalBookingEntity = bookingRepository.findById(bookingId);
        if (optionalBookingEntity.isEmpty()) {
            throw new NotFoundException(String.format("Бронирование id=%s не найдено!", bookingId));
        }
        if (!userId.equals(optionalBookingEntity.get().getItemEntity().getUserEntity().getId())) {
            throw new NotFoundException("Бронирование может редактировать только хозяин вещи!");
        }
        if (!optionalBookingEntity.get().getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронирование уже рассмотрено хозяином вещи!");
        }
        BookingEntity bookingEntity = optionalBookingEntity.get();
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

    public BookingDto getBooking(Long userId, Long bookingId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", userId));
        }
        Optional<BookingEntity> optionalBookingEntity = bookingRepository.findById(bookingId);
        if (optionalBookingEntity.isEmpty()) {
            throw new NotFoundException(String.format("Бронирование id=%s не найдено!", userId));
        }
        if (!userId.equals(optionalBookingEntity.get().getUserEntity().getId()) &&
                !userId.equals(optionalBookingEntity.get().getItemEntity().getUserEntity().getId())) {
            throw new NotFoundException("Бронирование может запросить хозяин вещи или бронирующий!");
        }
        return mapBookingEntityToBookingDto(optionalBookingEntity.get());
    }

    public List<BookingDto> getBookingsByBookerId(Long bookerId, String state) {
        if (userRepository.findById(bookerId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", bookerId));
        }
        try {
            State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        Stream<BookingEntity> bookingEntityStream;
        switch (State.valueOf(state)) {
            case ALL:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStatus().equals(Status.WAITING));
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStatus().equals(Status.REJECTED));
                break;
            case PAST:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream()
                        .filter(bookingEntity -> bookingEntity.getEndTime().isBefore(LocalDateTime.now()));
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStartTime().isBefore(LocalDateTime.now()) &&
                                bookingEntity.getEndTime().isAfter(LocalDateTime.now()));
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository.findByUserEntityId(bookerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStartTime().isAfter(LocalDateTime.now()));
                break;
            default:
                bookingEntityStream = Stream.empty();
        }
        return bookingEntityStream
                .sorted()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByOwnerId(Long ownerId, String state) {
        if (userRepository.findById(ownerId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден!", ownerId));
        }
        try {
            State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", state));
        }
        Stream<BookingEntity> bookingEntityStream;
        switch (State.valueOf(state)) {
            case ALL:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream();
                break;
            case WAITING:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStatus().equals(Status.WAITING));
                break;
            case REJECTED:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStatus().equals(Status.REJECTED));
                break;
            case PAST:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream()
                        .filter(bookingEntity -> bookingEntity.getEndTime().isBefore(LocalDateTime.now()));
                break;
            case CURRENT:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStartTime().isBefore(LocalDateTime.now())
                                && bookingEntity.getEndTime().isAfter(LocalDateTime.now()));
                break;
            case FUTURE:
                bookingEntityStream = bookingRepository.findByItemEntityUserEntityId(ownerId).stream()
                        .filter(bookingEntity -> bookingEntity.getStartTime().isAfter(LocalDateTime.now()));
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
