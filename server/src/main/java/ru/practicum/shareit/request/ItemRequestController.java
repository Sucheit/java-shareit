package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestController {

    ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        ItemRequestDto addedRequest = itemRequestService.addItemRequest(itemRequestDto, userId);
        log.info("Добавлен запрос: {}", addedRequest);
        return addedRequest;
    }

    @GetMapping
    public Iterable<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByUserId(userId);
        log.info("Получен список запросов пользователя id={}, размер = {}", userId, itemRequests.size());
        return itemRequests;
    }

    @GetMapping(path = "/all")
    public Iterable<ItemRequestDto> getItemRequestsByOtherUsers(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByOtherUsers(userId, from, size);
        log.info("Получен список запросов пользователя id={}, размер = {}", userId, itemRequests.size());
        return itemRequests;
    }

    @GetMapping(path = "/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long requestId) {
        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(userId, requestId);
        log.info("Получен запрос предмета: {}", itemRequestDto);
        return itemRequestDto;
    }
}
