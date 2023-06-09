package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    ItemService itemService;

    @PostMapping()
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        ItemDto addedItem = itemService.addItem(userId, itemDto);
        log.info("Добавили Item: {}", addedItem);
        return addedItem;
    }

    @PatchMapping(value = "/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDtoUpdate itemDto, @PathVariable Long itemId) {
        ItemDto updatedItem = itemService.updateItem(userId, itemDto, itemId);
        log.info("Обновили Item: {}", updatedItem);
        return updatedItem;
    }

    @GetMapping(value = "/{itemId}")
    public ItemDto getItemById(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                               @PathVariable long itemId) {
        ItemDto itemDto = itemService.getItemById(itemId, userId);
        log.info("Получили Item: {}", itemDto);
        return itemDto;
    }

    @GetMapping()
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(required = false, defaultValue = "0") int from,
                                          @RequestParam(required = false, defaultValue = "20") int size) {
        List<ItemDto> items = itemService.findAllByUserId(userId, from, size);
        log.info("Получили список Items: size()={}", items.size());
        return items;
    }

    @GetMapping(value = "/search")
    public List<ItemDto> findBySearchTerm(@RequestParam(value = "text") String text,
                                          @RequestParam(required = false, defaultValue = "0") int from,
                                          @RequestParam(required = false, defaultValue = "20") int size) {
        List<ItemDto> items = itemService.findByNameOrDescription(text, from, size);
        log.info("Получили список size()={} по фильтру: {}", items.size(), text);
        return items;
    }

    @PostMapping(value = "/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(value = "X-Sharer-User-Id") long userId,
                                 @PathVariable long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        CommentDto addedComment = itemService.addComment(userId, itemId, commentDto);
        log.info("Пользователь id={} добавил комментарий вещи id={}: {}", userId, itemId, addedComment);
        return addedComment;
    }
}
