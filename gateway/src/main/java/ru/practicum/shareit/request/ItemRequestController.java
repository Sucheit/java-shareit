package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestController {

    ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("User id={} creating ItemRequest: {}", userId, itemRequestDto);
        return itemRequestClient.createItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("User id={} getting ItemRequests", userId);
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getItemRequestsByOtherUsers(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("User id={} getting requests by other users, from={}, size={}", userId, from, size);
        return itemRequestClient.getItemRequestsByOtherUsers(userId, from, size);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @PathVariable long requestId) {
        log.info("User id={} getting ItemRequest id={}", userId, requestId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
