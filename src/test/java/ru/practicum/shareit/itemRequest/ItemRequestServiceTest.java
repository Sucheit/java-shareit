package ru.practicum.shareit.itemRequest;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestServiceTest {

    @InjectMocks
    ItemRequestService itemRequestService;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ItemRepository itemRepository;

    @Test
    public void testGetItemRequestById_givenValidIds_expectSuccess() {
        Optional<User> userOptional = Optional.of(User.builder().id(4L).name("user").email("user@mail.com").build());
        Optional<ItemRequest> itemRequestOptional = Optional.of(ItemRequest.builder().id(5L).description("desc")
                .created(LocalDateTime.now()).user(userOptional.get()).build());
        when(userRepository.findById(4L)).thenReturn(userOptional);
        when(itemRequestRepository.findById(5L)).thenReturn(itemRequestOptional);
        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(4L, 5L);
        assertNotNull(itemRequestDto);
        assertEquals(5L, itemRequestDto.getId());
        assertEquals("desc", itemRequestDto.getDescription());
        assertEquals(0, itemRequestDto.getItems().size());
    }

    @Test
    public void testGetItemRequestById_givenInvalidUserId_thenExpectNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestById(1L, 5L));
    }

    @Test
    public void testGetItemRequestById_givenInvalidRequestId_thenExpectNotFound() {
        Optional<User> userOptional = Optional.of(User.builder().id(4L).name("user").email("user@mail.com").build());
        when(userRepository.findById(4L)).thenReturn(userOptional);
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestById(4L, 5L));
    }

    @Test
    public void testGetItemRequestsByUserId_givenValidUserId_thenSuccess() {
        Optional<User> userOptional = Optional.of(User.builder().id(4L).name("user").email("user@mail.com").build());
        when(userRepository.findById(4L)).thenReturn(userOptional);
        List<ItemRequest> itemRequestList = List.of(ItemRequest.builder().description("desc").user(userOptional.get())
                .created(LocalDateTime.now()).build());
        when(itemRequestRepository.findByUserIdOrderByCreatedAsc(4L)).thenReturn(itemRequestList);
        List<ItemRequestDto> itemRequestDtoList = itemRequestService.getItemRequestsByUserId(4L);
        assertNotNull(itemRequestDtoList);
        assertEquals(1, itemRequestDtoList.size());
        assertEquals("desc", itemRequestDtoList.get(0).getDescription());
        assertEquals(userOptional.get().getId(), itemRequestDtoList.get(0).getUserId());
    }

    @Test
    public void testGetItemRequestsByUserId_givenInvalidUserId_thenExpectNotFound() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemRequestService.getItemRequestsByUserId(4L));
    }

    @Test
    public void testAddItemRequest_givenInvalidUserId_thenExpectNotFound() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> itemRequestService.addItemRequest(ItemRequestDto.builder().build(), 4L));
    }

    @Test
    public void testAddItemRequest_givenValidUserId_thenExpectSuccess() {
        Optional<User> userOptional = Optional.of(User.builder().id(4L).name("user").email("user@mail.com").build());
        when(userRepository.findById(4L)).thenReturn(userOptional);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(ItemRequest.builder().id(1L)
                .user(userOptional.get()).created(LocalDateTime.now()).description("desc").build());
        ItemRequestDto itemRequestDto = itemRequestService.addItemRequest(
                ItemRequestDto.builder().description("desc").build(), 4L);
        assertNotNull(itemRequestDto);
        assertEquals(1, itemRequestDto.getId());
        assertEquals(4, itemRequestDto.getUserId());
    }

    @Test
    public void testGetItemRequestsByOtherUsers_givenInvalidUserId_thenExpectNotFound() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestsByOtherUsers(4L, 0, 20));
    }

    @Test
    public void testGetItemRequestsByOtherUsers_givenInvalidUserId_thenExpectSuccess() {
        Optional<User> userOptional = Optional.of(User.builder().id(4L).name("user").email("user@mail.com").build());
        when(userRepository.findById(4L)).thenReturn(userOptional);
        List<ItemRequest> itemRequestList = List.of(ItemRequest.builder().id(3L).description("desc")
                .created(LocalDateTime.now()).user(User.builder().name("name").email("name@email.com")
                        .id(2L).build()).build());
        when(itemRequestRepository.findByUserIdNotOrderByCreatedAsc(4L, PageRequest.of(0, 20)))
                .thenReturn(itemRequestList);
        List<ItemRequestDto> itemRequestsDtoList = itemRequestService
                .getItemRequestsByOtherUsers(4L, 0, 20);
        assertNotNull(itemRequestsDtoList);
        assertEquals(1, itemRequestsDtoList.size());
        assertEquals(3, itemRequestList.get(0).getId());
        assertEquals(2, itemRequestList.get(0).getUser().getId());
    }
}
