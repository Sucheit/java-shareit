package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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

    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        itemDto.setUserEntity(userEntity);
        ItemEntity itemEntity = mapItemDtoToItemEntity(itemDto);
        return mapItemEntityToItemDto(itemRepository.save(itemEntity));
    }

    @Transactional
    public ItemDto updateItem(Long userId, ItemDtoUpdate itemDtoUpdate, Long itemId) {
        ItemEntity itemEntity = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        if (!userId.equals(itemEntity.getUserEntity().getId())) {
            throw new ForbiddenException(String.format("Пользователь id=%s не соответствует вещи", userId));
        }
        ItemEntity itemToUpdate = mapItemDtoToItemEntity(itemDtoUpdate);
        itemToUpdate.setUserEntity(userEntity);
        itemToUpdate.setId(itemId);
        if (itemDtoUpdate.getName() == null) {
            itemToUpdate.setName(itemEntity.getName());
        }
        if (itemDtoUpdate.getDescription() == null) {
            itemToUpdate.setDescription(itemEntity.getDescription());
        }
        if (itemDtoUpdate.getAvailable() == null) {
            itemToUpdate.setAvailable(itemEntity.getAvailable());
        }
        return mapItemEntityToItemDto(itemRepository.save(itemToUpdate));
    }

    @Transactional
    public ItemDto getItemById(Long itemId, Long userId) {
        ItemEntity itemEntity = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        ItemDto itemDto = mapItemEntityToItemDto(itemEntity);
        if (userId != null && userId.equals(itemEntity.getUserEntity().getId())) {
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

    @Transactional
    public List<ItemDto> findAllByUserId(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        return itemRepository.findByUserEntityId(userId).stream()
                .map(ItemMapper::mapItemEntityToItemDto)
                .peek(itemDto -> setLastAndNextBookings.accept(itemDto,
                        bookingRepository.findByItemEntityId(itemDto.getId())))
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ItemDto> findByNameOrDescription(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text).stream()
                .filter(itemEntity -> itemEntity.getAvailable().equals(Boolean.TRUE))
                .map(ItemMapper::mapItemEntityToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        ItemEntity itemEntity = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        if (bookingRepository.findByItemEntityIdAndUserEntityId(itemId, userId).stream()
                .filter(bookingEntity -> bookingEntity.getUserEntity().getId().equals(userId) &&
                        bookingEntity.getEndTime().isBefore(LocalDateTime.now()))
                .findAny().isEmpty()) {
            throw new BadRequestException("Пользователь не бронировал вещь");
        }
        CommentEntity commentEntity = CommentEntity.builder()
                .created(LocalDateTime.now())
                .text(commentDto.getText())
                .userEntity(userEntity)
                .itemEntity(itemEntity)
                .build();
        CommentEntity addedComment = commentRepository.save(commentEntity);
        return mapCommentEntityToCommentDto(addedComment);
    }
}
