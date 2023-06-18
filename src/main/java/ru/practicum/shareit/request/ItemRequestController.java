package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestController {

    ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @Valid @RequestBody ItemRequestDto itemRequestDto) {
        ItemRequestDto addedRequest = itemRequestService.addItemRequest(itemRequestDto, userId);
        log.info("Добавлен запрос: {}", addedRequest);
        return itemRequestService.addItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public Iterable<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        Iterable<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByUserId(userId);
        log.info("Получен список запросов пользователя id={}, размер = {}", userId, itemRequests);
        return itemRequests;
    }

    @GetMapping(path = "/all")
    public Iterable<ItemRequestDto> getItemRequestsFromAndSize(@RequestHeader("X-Sharer-User-Id") long userId,
                                                               @RequestParam(required = false) Integer from,
                                                               @RequestParam(required = false) Integer size) {
        Iterable<ItemRequestDto> itemRequests = itemRequestService.getItemRequestsByUserIdWithPages(userId, from, size);
        log.info("Получен список запросов пользователя id={}, размер = {}", userId, itemRequests);
        return itemRequests;
    }


}
