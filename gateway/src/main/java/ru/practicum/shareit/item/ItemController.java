package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    ItemClient itemClient;

    @PostMapping()
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info(" Item: {}", itemDto);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping(value = "/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody ItemDtoUpdate itemDtoUpdate,
                                             @PathVariable long itemId) {
        log.info("User id={} patching Item id={}: {}", userId, itemId, itemDtoUpdate);
        return itemClient.updateItem(userId, itemId, itemDtoUpdate);
    }

    @GetMapping(value = "/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                              @PathVariable long itemId) {
        log.info("User id={} getting Item id={}", userId, itemId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> getItemsByUserId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("User id={} getting items, from={}, size={}", userId, from, size);
        return itemClient.getItemsByUser(userId, from, size);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Object> findBySearchTerm(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "text") String text,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Searching items by: {}, from={}, size={}", text, from, size);
        return itemClient.getItemsBySearchTerm(userId, text, from, size);
    }

    @PostMapping(value = "/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                                @PathVariable long itemId,
                                                @Valid @RequestBody CommentDto commentDto) {
        log.info("User id={} adding comment to Item id={}: {}", userId, itemId, commentDto);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
