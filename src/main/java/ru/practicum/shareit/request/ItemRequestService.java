package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.ItemRequestMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.model.ItemRequestMapper.mapDtoToItemRequest;
import static ru.practicum.shareit.request.model.ItemRequestMapper.mapItemRequestToDto;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestService {

    ItemRequestRepository itemRequestRepository;

    UserRepository userRepository;

    public Iterable<ItemRequestDto> getItemRequestsByUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        return itemRequestRepository.findAllByUserIdOrderByIdAsc(user.getId()).stream()
                .map(ItemRequestMapper::mapItemRequestToDto)
                .collect(Collectors.toList());
    }

    public Iterable<ItemRequestDto> getItemRequestsByUserIdWithPages(long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        if (from == null || size == null) {
            return itemRequestRepository.findAllByUserIdOrderByIdAsc(user.getId()).stream()
                    .map(ItemRequestMapper::mapItemRequestToDto)
                    .collect(Collectors.toList());
        } else {
            if (from < 0 || size < 1) {
                throw new BadRequestException("Не верные параметры from или size");
            }
            return itemRequestRepository.findAllByUserIdOrderByIdAsc(user.getId(), PageRequest.of(from, size)).stream()
                    .map(ItemRequestMapper::mapItemRequestToDto)
                    .collect(Collectors.toList());
        }
    }

    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id=%s не найден.", userId)));
        ItemRequest itemRequest = mapDtoToItemRequest(itemRequestDto);
        itemRequest.setUser(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestRepository.save(itemRequest);
        return mapItemRequestToDto(itemRequest);
    }
}
