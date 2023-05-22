package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.model.ItemMapper.mapItemDtoToItemEntity;
import static ru.practicum.shareit.item.model.ItemMapper.mapItemEntityToItemDto;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;

    public ItemDto addItem(Long userId, ItemDto itemDto) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", userId));
        }
        itemDto.setUserEntity(optionalUserEntity.get());
        ItemEntity itemEntity = mapItemDtoToItemEntity(itemDto);
        return mapItemEntityToItemDto(itemRepository.save(itemEntity));
    }

    public ItemDto updateItem(Long userId, ItemDtoUpdate itemDtoUpdate, Long itemId) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id='%s' не найдена", itemId));
        }
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", userId));
        }
        if (!userId.equals(itemEntityOptional.get().getUserEntity().getId())) {
            throw new ForbiddenException(String.format("Пользователь id='%s' не соответствует вещи", userId));
        }
        ItemEntity itemToUpdate = mapItemDtoToItemEntity(itemDtoUpdate);
        itemToUpdate.setUserEntity(optionalUserEntity.get());
        itemToUpdate.setId(itemId);
        if (itemDtoUpdate.getName() == null) {
            itemToUpdate.setName(itemEntityOptional.get().getName());
        }
        if (itemDtoUpdate.getDescription() == null) {
            itemToUpdate.setDescription(itemEntityOptional.get().getDescription());
        }
        if (itemDtoUpdate.getAvailable() == null) {
            itemToUpdate.setAvailable(itemEntityOptional.get().getAvailable());
        }
        return mapItemEntityToItemDto(itemRepository.save(itemToUpdate));
    }

    public ItemDto getItemById(Long itemId) {
        Optional<ItemEntity> itemEntityOptional = itemRepository.findById(itemId);
        if (itemEntityOptional.isEmpty()) {
            throw new NotFoundException(String.format("Вещь id='%s' не найдена", itemId));
        }
        return mapItemEntityToItemDto(itemEntityOptional.get());
    }

    public List<ItemDto> findAllByUserId(Long userId) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", userId));
        }
        return itemRepository.findByUserEntityId(userId).stream()
                .map(ItemMapper::mapItemEntityToItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> findByNameOrDescription(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text).stream()
                .filter(itemEntity -> itemEntity.getAvailable().equals(Boolean.TRUE))
                .map(ItemMapper::mapItemEntityToItemDto)
                .collect(Collectors.toList());
    }
}
