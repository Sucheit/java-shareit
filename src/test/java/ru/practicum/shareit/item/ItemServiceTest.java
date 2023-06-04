package ru.practicum.shareit.item;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.ItemEntity;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Test
    public void getItemById() {
        Optional<ItemEntity> item = Optional.of(ItemEntity.builder().id(1L).name("name").description("desc")
                .userEntity(UserEntity.builder().id(3L).build()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(item);
        ItemDto retrievedItem = itemService.getItemById(1L, 1L);
        assertNotNull(retrievedItem);
        assertEquals(1, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
        assertEquals("desc", retrievedItem.getDescription());
    }

    @Test
    public void findAllByUserId() {
        Optional<UserEntity> user = Optional.of(UserEntity.builder().id(1L).name("name").build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(user);
        List<ItemEntity> items = List.of(ItemEntity.builder().id(3L).name("name1").build(),
                ItemEntity.builder().id(5L).name("name2").build());
        when(itemRepository.findByUserEntityId(user.get().getId())).thenReturn(items);
        when(bookingRepository.findByItemEntityId(Mockito.anyLong())).thenReturn(Collections.emptyList());
        List<ItemDto> retrievedItems = itemService.findAllByUserId(1L);
        assertNotNull(retrievedItems);
        assertEquals(2, retrievedItems.size());
        assertEquals(3, retrievedItems.get(0).getId());
        assertEquals(5, retrievedItems.get(1).getId());

        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.findAllByUserId(Mockito.anyLong()));
    }

    @Test
    public void addItem_givenValidItem_thenSucceed() {
        Optional<UserEntity> user = Optional.of(UserEntity.builder().id(1L).build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(user);
        ItemEntity item = ItemEntity.builder().id(7L).name("name").build();
        ItemDto itemDto = ItemDto.builder().id(7L).name("name").build();
        when(itemRepository.save(Mockito.any(ItemEntity.class))).thenReturn(item);
        ItemDto retrievedItem = itemService.addItem(Mockito.anyLong(), itemDto);
        assertNotNull(retrievedItem);
        assertEquals(7, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());

        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addItem(Mockito.anyLong(), itemDto));
    }

    @Test
    public void addItem_givenInvalidUser_thenExceptNotFoundException() {
        ItemDto itemDto = ItemDto.builder().id(7L).name("name").build();
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addItem(Mockito.anyLong(), itemDto));
    }

    @Test
    public void updateItem_givenValidItem_thenSucceed() {
        Optional<UserEntity> user = Optional.of(UserEntity.builder().id(1L).build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(user);
        Optional<ItemEntity> item = Optional.of(ItemEntity.builder().id(7L).name("name")
                .userEntity(UserEntity.builder().id(1L).build()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(item);
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        when(itemRepository.save(Mockito.any(ItemEntity.class))).thenReturn(item.get());
        ItemDto retrievedItem = itemService.updateItem(1L, itemDtoUpdate, Mockito.anyLong());
        assertNotNull(retrievedItem);
        assertEquals(7, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
    }

    @Test
    public void updateItem_givenConflictItemIdAndUserId_thenExceptForbiddenException() {
        Optional<UserEntity> user = Optional.of(UserEntity.builder().id(1L).build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(user);
        Optional<ItemEntity> item = Optional.of(ItemEntity.builder().id(7L).name("name")
                .userEntity(UserEntity.builder().id(2L).build()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(item);
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(ForbiddenException.class, () -> itemService.updateItem(1L, itemDtoUpdate, Mockito.anyLong()));
    }

    @Test
    public void updateItem_givenInvalidUserId_thenExceptNotFoundException() {
        Optional<ItemEntity> item = Optional.of(ItemEntity.builder().id(7L).name("name")
                .userEntity(UserEntity.builder().id(1L).build()).build());
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(item);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, itemDtoUpdate, Mockito.anyLong()));
    }

    @Test
    public void updateItem_givenInvalidItemId_thenExceptNotFoundException() {
        when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, itemDtoUpdate, Mockito.anyLong()));
    }

    @Test
    public void findByNameOrDescription_thenSucceed() {
        List<ItemEntity> itemEntities = List.of(
                ItemEntity.builder().id(10L).name("hammer").available(Boolean.TRUE).build());
        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                Mockito.anyString(), Mockito.anyString())).thenReturn(itemEntities);
        List<ItemDto> items = itemService.findByNameOrDescription("hammer");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(10, items.get(0).getId());
        assertEquals("hammer", items.get(0).getName());
    }
}
