package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingEntity;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.CommentEntity;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.model.ItemMapper.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    public ItemDto addItem(Long userId, ItemDto itemDto) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        itemDto.setUserEntity(optionalUserEntity.get());
        ItemEntity itemEntity = mapItemDtoToItemEntity(itemDto);
        return mapItemEntityToItemDto(itemRepository.save(itemEntity));
    }

    public ItemDto updateItem(Long userId, ItemDtoUpdate itemDtoUpdate, Long itemId) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id=%s не найдена", itemId));
        }
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        if (!userId.equals(itemEntityOptional.get().getUserEntity().getId())) {
            throw new ForbiddenException(String.format("Пользователь id=%s не соответствует вещи", userId));
        }
        ItemEntity itemToUpdate = mapItemDtoToItemEntity(itemDtoUpdate);
        itemToUpdate.setUserEntity(optionalUserEntity.get());
        itemToUpdate.setId(itemId);
        if (itemDtoUpdate.getName() == null) {
            itemToUpdate.setName(itemEntityOptional.get().getName());
        }
        if (itemDtoUpdate.getDescription() == null) {
            itemToUpdate.setDescription(itemEntityOptional.get().getDescription());
        }
        if (itemDtoUpdate.getAvailable() == null) {
            itemToUpdate.setAvailable(itemEntityOptional.get().getAvailable());
        }
        return mapItemEntityToItemDto(itemRepository.save(itemToUpdate));
    }

    public ItemDto getItemById(Long itemId, Long userId) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id=%s не найдена", itemId));
        }
        ItemDto itemDto = mapItemEntityToItemDto(itemEntityOptional.get());
        if (userId != null && userId.equals(itemEntityOptional.get().getUserEntity().getId())) {
            if (userRepository.findById(userId).isEmpty()) {
                throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
            }
            List<BookingEntity> bookingEntities = bookingRepository.findByItemEntityId(itemId);
            setLastAndNextBookings.accept(itemDto, bookingEntities);
        }
        Set<CommentDto> comments = commentRepository.findByItemEntityId(itemId).stream()
                .map(ItemMapper::mapCommentEntityToCommentDto)
                .collect(Collectors.toSet());
        itemDto.setComments(comments);
        return itemDto;
    }

    private final BiConsumer<ItemDto, List<BookingEntity>> setLastAndNextBookings = (itemDto, bookingEntities) -> {
        bookingEntities.stream()
                .sorted()
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .filter(bookingDto -> bookingDto.getStart().isBefore(LocalDateTime.now()) &&
                        bookingDto.getStatus().equals(Status.APPROVED))
                .findFirst()
                .ifPresent(bookingDto -> itemDto.setLastBooking(mapBookingDtoToItemBooking(bookingDto)));
        bookingEntities.stream()
                .sorted(Comparator.reverseOrder())
                .map(BookingMapper::mapBookingEntityToBookingDto)
                .filter(bookingDto -> bookingDto.getStart().isAfter(LocalDateTime.now()) &&
                        bookingDto.getStatus().equals(Status.APPROVED))
                .findFirst()
                .ifPresent(bookingDto -> itemDto.setNextBooking(mapBookingDtoToItemBooking(bookingDto)));
    };

    public List<ItemDto> findAllByUserId(Long userId) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        return itemRepository.findByUserEntityId(userId).stream()
                .map(ItemMapper::mapItemEntityToItemDto)
                .peek(itemDto -> setLastAndNextBookings.accept(itemDto, bookingRepository.findByItemEntityId(itemDto.getId())))
                .sorted()
                .collect(Collectors.toList());
    }

    public List<ItemDto> findByNameOrDescription(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text).stream()
                .filter(itemEntity -> itemEntity.getAvailable().equals(Boolean.TRUE))
                .map(ItemMapper::mapItemEntityToItemDto)
                .collect(Collectors.toList());
    }

    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id=%s не найдена", itemId));
        }
        if (bookingRepository.findByItemEntityIdAndUserEntityId(itemId, userId).stream()
                .filter(bookingEntity -> bookingEntity.getUserEntity().getId().equals(userId) &&
                        bookingEntity.getEndTime().isBefore(LocalDateTime.now()))
                .findAny().isEmpty()) {
            throw new BadRequestException("Пользователь не бронировал вещь");
        }
        CommentEntity commentEntity = CommentEntity.builder()
                .created(LocalDateTime.now())
                .text(commentDto.getText())
                .userEntity(optionalUserEntity.get())
                .itemEntity(itemEntityOptional.get())
                .build();
        CommentEntity addedComment = commentRepository.save(commentEntity);
        return mapCommentEntityToCommentDto(addedComment);
    }
}
