package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemServiceTest {

    final PageRequest pageRequest = PageRequest.of(0, 20);
    @InjectMocks
    ItemService itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Test
    public void getItemById_givenValidData_expectSuccess() {
        User user = User.builder().id(3L).build();
        Item item = Item.builder().id(1L).name("name").description("desc").user(user).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(userRepository.existsById(1L)).thenReturn(true);

        ItemDto retrievedItem = itemService.getItemById(1L, 1L);

        assertNotNull(retrievedItem);
        assertEquals(1, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
        assertEquals("desc", retrievedItem.getDescription());
    }

    @Test
    public void getItemById_givenRequestByOwner_expectSuccess() {
        User user = User.builder().id(3L).build();
        Item item = Item.builder().id(1L).name("name").description("desc").user(user).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(userRepository.existsById(3L)).thenReturn(true);

        ItemDto retrievedItem = itemService.getItemById(1L, 3L);

        assertNotNull(retrievedItem);
        assertEquals(1, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
        assertEquals("desc", retrievedItem.getDescription());
    }

    @Test
    public void getItemById_givenInvalidItemId_expectNotFound() {
        when(userRepository.existsById(3L)).thenReturn(true);
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 3L));
    }

    @Test
    public void getItemById_givenInvalidUserId_expectNotFound() {
        when(userRepository.existsById(3L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 3L));
    }

    @Test
    public void findAllByUserId_givenValidUserId_expectSuccess() {
        Optional<User> user = Optional.of(User.builder().id(1L).name("name").build());
        when(userRepository.existsById(anyLong())).thenReturn(true);
        List<Item> items = List.of(Item.builder().id(3L).name("name1").build(),
                Item.builder().id(5L).name("name2").build());
        when(itemRepository.findByUserIdOrderByIdAsc(user.get().getId(), pageRequest)).thenReturn(items);

        List<ItemDto> retrievedItems = itemService.findAllByUserId(1L, 0, 20);

        assertNotNull(retrievedItems);
        assertEquals(2, retrievedItems.size());
        assertEquals(3, retrievedItems.get(0).getId());
        assertEquals(5, retrievedItems.get(1).getId());

    }

    @Test
    public void findAllByUserId_givenInvalidUserId_expectSuccess() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.findAllByUserId(1L, 0, 20));
    }

    @Test
    public void addItem_givenValidItem_thenSucceed() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
        Item item = Item.builder().id(7L).name("name").build();
        ItemDto itemDto = ItemDto.builder().id(7L).name("name").build();
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto retrievedItem = itemService.addItem(anyLong(), itemDto);

        assertNotNull(retrievedItem);
        assertEquals(7, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
    }

    @Test
    public void addItem_givenValidItemWithRequest_thenSucceed() {
        User itemOwner = User.builder().id(1L).name("itemOwner").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(itemOwner));
        User user = User.builder().id(2L).name("user").build();
        ItemRequest itemRequest = ItemRequest.builder().id(1L).description("desc").user(user).build();
        Item item = Item.builder().id(7L).name("item").itemRequest(itemRequest).build();
        ItemDto itemDto = ItemDto.builder().id(7L).name("name").requestId(1L).build();
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));

        ItemDto retrievedItem = itemService.addItem(1L, itemDto);

        assertNotNull(retrievedItem);
        assertEquals(7, retrievedItem.getId());
        assertEquals("item", retrievedItem.getName());
        assertEquals(1, retrievedItem.getRequestId());
    }

    @Test
    public void addItem_givenInvalidUser_thenExceptNotFoundException() {
        ItemDto itemDto = ItemDto.builder().id(7L).name("name").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(anyLong(), itemDto));
    }

    @Test
    public void updateItem_givenValidItem_thenSucceed() {
        Optional<User> user = Optional.of(User.builder().id(1L).build());
        when(userRepository.findById(anyLong())).thenReturn(user);
        Optional<Item> item = Optional.of(Item.builder().id(7L).name("name")
                .user(User.builder().id(1L).build()).build());
        when(itemRepository.findById(anyLong())).thenReturn(item);
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        when(itemRepository.save(Mockito.any(Item.class))).thenReturn(item.get());
        ItemDto retrievedItem = itemService.updateItem(1L, itemDtoUpdate, anyLong());
        assertNotNull(retrievedItem);
        assertEquals(7, retrievedItem.getId());
        assertEquals("name", retrievedItem.getName());
    }

    @Test
    public void updateItem_givenConflictItemIdAndUserId_thenExceptForbiddenException() {
        Optional<User> user = Optional.of(User.builder().id(1L).build());
        when(userRepository.findById(anyLong())).thenReturn(user);
        Optional<Item> item = Optional.of(Item.builder().id(7L).name("name")
                .user(User.builder().id(2L).build()).build());
        when(itemRepository.findById(anyLong())).thenReturn(item);
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(ForbiddenException.class, () -> itemService.updateItem(1L, itemDtoUpdate, anyLong()));
    }

    @Test
    public void updateItem_givenInvalidUserId_thenExceptNotFoundException() {
        Optional<Item> item = Optional.of(Item.builder().id(7L).name("name")
                .user(User.builder().id(1L).build()).build());
        when(itemRepository.findById(anyLong())).thenReturn(item);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, itemDtoUpdate, anyLong()));
    }

    @Test
    public void updateItem_givenInvalidItemId_thenExceptNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(7L).name("name").build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, itemDtoUpdate, anyLong()));
    }

    @Test
    public void findByNameOrDescription_thenSucceed() {
        List<Item> itemList = List.of(
                Item.builder().id(10L).name("hammer").available(Boolean.TRUE).build());
        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "hammer", "hammer", pageRequest)).thenReturn(itemList);
        List<ItemDto> items = itemService.findByNameOrDescription("hammer", 0, 20);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(10, items.get(0).getId());
        assertEquals("hammer", items.get(0).getName());
    }

    @Test
    public void findByNameOrDescription_givenTextIsBlank_thenGettingEmptyList() {
        List<ItemDto> items = itemService.findByNameOrDescription("", 0, 20);

        assertNotNull(items);
        assertEquals(0, items.size());
    }

    @Test
    public void addComment_givenValidData_expectSuccess() {
        User owner = User.builder().id(3L).name("owner").build();
        Item item = Item.builder().id(4L).name("item").user(owner).build();
        User user = User.builder().id(5L).name("user").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(4L)).thenReturn(Optional.of(item));
        Booking booking = Booking.builder().item(item).user(user).endTime(LocalDateTime.now().minusHours(1)).build();
        when(bookingRepository.findByItemIdAndUserIdOrderByStartTimeDesc(4L, 5L))
                .thenReturn(List.of(booking));
        Comment comment = Comment.builder().id(8L).text("comment").item(item).user(user).build();
        CommentDto commentDto = CommentDto.builder().text("comment").build();
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto addedComment = itemService.addComment(5L, 4L, commentDto);

        assertNotNull(addedComment);
        assertEquals(8, addedComment.getId());
        assertEquals("user", addedComment.getAuthorName());
        assertEquals("comment", addedComment.getText());
    }

    @Test
    public void addComment_givenInvalidBooking_expectBadRequest() {
        User owner = User.builder().id(3L).name("owner").build();
        Item item = Item.builder().id(4L).name("item").user(owner).build();
        User user = User.builder().id(5L).name("user").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(4L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndUserIdOrderByStartTimeDesc(4L, 5L))
                .thenReturn(Collections.emptyList());
        CommentDto commentDto = CommentDto.builder().text("comment").build();

        assertThrows(BadRequestException.class, () -> itemService.addComment(5L, 4L, commentDto));
    }

    @Test
    public void addComment_givenInvalidItem_expectNotFound() {
        User user = User.builder().id(5L).name("user").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(4L)).thenReturn(Optional.empty());
        CommentDto commentDto = CommentDto.builder().text("comment").build();

        assertThrows(NotFoundException.class, () -> itemService.addComment(5L, 4L, commentDto));
    }

    @Test
    public void addComment_givenInvalidUser_expectNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        CommentDto commentDto = CommentDto.builder().text("comment").build();

        assertThrows(NotFoundException.class, () -> itemService.addComment(5L, 4L, commentDto));
    }
}
