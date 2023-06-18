package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

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

    private final BiConsumer<ItemDto, List<Booking>> setLastAndNextBookings = (itemDto, bookingEntities) -> {
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
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        Item item = mapItemDtoToItemEntity(itemDto);
        item.setUser(user);
        return mapItemEntityToItemDto(itemRepository.save(item));
    }

    @Transactional
    public ItemDto updateItem(Long userId, ItemDtoUpdate itemDtoUpdate, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        if (!userId.equals(item.getUser().getId())) {
            throw new ForbiddenException(String.format("Пользователь id=%s не соответствует вещи", userId));
        }
        Item itemToUpdate = mapItemDtoToItemEntity(itemDtoUpdate);
        itemToUpdate.setUser(user);
        itemToUpdate.setId(itemId);
        if (itemDtoUpdate.getName() == null) {
            itemToUpdate.setName(item.getName());
        }
        if (itemDtoUpdate.getDescription() == null) {
            itemToUpdate.setDescription(item.getDescription());
        }
        if (itemDtoUpdate.getAvailable() == null) {
            itemToUpdate.setAvailable(item.getAvailable());
        }
        return mapItemEntityToItemDto(itemRepository.save(itemToUpdate));
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        ItemDto itemDto = mapItemEntityToItemDto(item);
        if (userId != null && userId.equals(item.getUser().getId())) {
            if (userRepository.findById(userId).isEmpty()) {
                throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
            }
            List<Booking> bookingEntities = bookingRepository.findByItemIdOrderByStartTimeDesc(itemId);
            setLastAndNextBookings.accept(itemDto, bookingEntities);
        }
        Set<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(ItemMapper::mapCommentEntityToCommentDto)
                .collect(Collectors.toSet());
        itemDto.setComments(comments);
        return itemDto;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllByUserId(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        return itemRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(ItemMapper::mapItemEntityToItemDto)
                .peek(itemDto -> setLastAndNextBookings.accept(itemDto,
                        bookingRepository.findByItemIdOrderByStartTimeDesc(itemDto.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        if (bookingRepository.findByItemIdAndUserIdOrderByStartTimeDesc(itemId, userId).stream()
                .filter(bookingEntity -> bookingEntity.getUser().getId().equals(userId) &&
                        bookingEntity.getEndTime().isBefore(LocalDateTime.now()))
                .findAny().isEmpty()) {
            throw new BadRequestException("Пользователь не бронировал вещь");
        }
        Comment comment = Comment.builder()
                .created(LocalDateTime.now())
                .text(commentDto.getText())
                .user(user)
                .item(item)
                .build();
        Comment addedComment = commentRepository.save(comment);
        return mapCommentEntityToCommentDto(addedComment);
    }
}
