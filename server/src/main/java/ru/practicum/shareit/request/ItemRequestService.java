package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static ru.practicum.shareit.exception.Validation.validatePagination;
import static ru.practicum.shareit.request.model.ItemRequestMapper.mapItemRequestToDto;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestService {

    ItemRequestRepository itemRequestRepository;

    UserRepository userRepository;

    ItemRepository itemRepository;

    BiConsumer<ItemRequestDto, ItemRepository> setItems = (itemRequestDto, repository) ->
            itemRequestDto.setItems((repository.findByItemRequestId(itemRequestDto.getId())).stream()
                    .map(ItemMapper::mapItemToItemDto).collect(Collectors.toList()));

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getItemRequestsByUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        return itemRequestRepository.findByUserIdOrderByCreatedAsc(user.getId()).stream()
                .map(ItemRequestMapper::mapItemRequestToDto)
                .peek(itemRequestDto -> setItems.accept(itemRequestDto, itemRepository))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getItemRequestsByOtherUsers(long userId, int from, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        validatePagination(from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return itemRequestRepository
                .findByUserIdNotOrderByCreatedAsc(user.getId(), pageRequest).stream()
                .map(ItemRequestMapper::mapItemRequestToDto)
                .peek(itemRequestDto -> setItems.accept(itemRequestDto, itemRepository))
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, long userId) {
        return mapItemRequestToDto(itemRequestRepository.save(ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .user(userRepository.findById(userId)
                        .orElseThrow(
                                () -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId))))
                .created(LocalDateTime.now())
                .build()));
    }

    @Transactional(readOnly = true)
    public ItemRequestDto getItemRequestById(long userId, long itemRequestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        ItemRequestDto itemRequestDto = mapItemRequestToDto(itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос предмета id=%s не найден.", itemRequestId))));
        setItems.accept(itemRequestDto, itemRepository);
        return itemRequestDto;
    }
}
