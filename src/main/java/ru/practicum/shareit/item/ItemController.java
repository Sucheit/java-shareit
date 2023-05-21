package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping()
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        ItemDto addedItem = itemService.addItem(userId, itemDto);
        log.info("Добавили Item: {}", addedItem);
        return addedItem;
    }

    @PatchMapping(value = "/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDtoUpdate itemDto, @PathVariable Long itemId) {
        ItemDto updatedItem = itemService.updateItem(userId, itemDto, itemId);
        log.info("Обновили Item: {}", updatedItem);
        return updatedItem;
    }

    @GetMapping(value = "/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        ItemDto itemDto = itemService.getItemById(itemId);
        log.info("Получили Item: {}", itemDto);
        return itemDto;
    }

    @GetMapping()
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.findAllByUserId(userId);
        log.info("Получили список Items: size()={}", items.size());
        return items;
    }

    @GetMapping(value = "/search")
    public List<ItemDto> findBySearchTerm(@RequestParam(value = "text") String text) {
        List<ItemDto> items = itemService.findByNameOrDescription(text);
        log.info("Получили список size()={} по фильтру: {}", items.size(), text);
        return items;
    }
}
