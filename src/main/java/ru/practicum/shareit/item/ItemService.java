package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
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
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static ru.practicum.shareit.exception.Validation.validatePagination;
import static ru.practicum.shareit.item.model.ItemMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemService {

    ItemRepository itemRepository;

    UserRepository userRepository;

    BookingRepository bookingRepository;

    CommentRepository commentRepository;

    ItemRequestRepository itemRequestRepository;
    BiConsumer<ItemDto, BookingRepository> setLastAndNextBookings = (itemDto, bookingRepository) -> {
        bookingRepository.findFirstByItemIdAndAndStartTimeBeforeAndStatusEqualsOrderByStartTimeDesc(
                        itemDto.getId(), LocalDateTime.now(), Status.APPROVED)
                .ifPresent(booking -> itemDto.setLastBooking(mapBookingDtoToItemBooking(booking)));
        bookingRepository.findFirstByItemIdAndAndStartTimeAfterAndStatusEqualsOrderByStartTimeAsc(
                        itemDto.getId(), LocalDateTime.now(), Status.APPROVED)
                .ifPresent(booking -> itemDto.setNextBooking(mapBookingDtoToItemBooking(booking)));
    };

    @Transactional
    public ItemDto addItem(long userId, ItemDto itemDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        Item item = mapItemDtoToItem(itemDto);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            item.setItemRequest(itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException(String.format("Запрос id=%s не найден", requestId))));
        }
        item.setUser(user);
        return mapItemToItemDto(itemRepository.save(item));
    }

    @Transactional
    public ItemDto updateItem(long userId, ItemDtoUpdate itemDtoUpdate, long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден", userId)));
        if (userId != item.getUser().getId()) {
            throw new ForbiddenException(String.format("Пользователь id=%s не соответствует вещи", userId));
        }
        Item itemToUpdate = mapItemDtoToItem(itemDtoUpdate);
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
        return mapItemToItemDto(itemRepository.save(itemToUpdate));
    }

    @Transactional(readOnly = true)
    public ItemDto getItemById(long itemId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь id=%s не найдена", itemId)));
        ItemDto itemDto = mapItemToItemDto(item);
        if (userId == item.getUser().getId()) {
            setLastAndNextBookings.accept(itemDto, bookingRepository);
        }
        Set<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(ItemMapper::mapCommentEntityToCommentDto)
                .collect(Collectors.toSet());
        itemDto.setComments(comments);
        return itemDto;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findAllByUserId(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь id=%s не найден", userId));
        }
        validatePagination(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return itemRepository.findByUserIdOrderByIdAsc(userId, pageRequest).stream()
                .map(ItemMapper::mapItemToItemDto)
                .peek(itemDto -> setLastAndNextBookings.accept(itemDto, bookingRepository))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemDto> findByNameOrDescription(String text, int from, int size) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        validatePagination(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text, pageRequest)
                .stream()
                .filter(itemEntity -> itemEntity.getAvailable().equals(Boolean.TRUE))
                .map(ItemMapper::mapItemToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
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
