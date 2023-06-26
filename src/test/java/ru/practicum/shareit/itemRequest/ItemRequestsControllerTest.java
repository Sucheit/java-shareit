package ru.practicum.shareit.itemRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void createItemRequest_whenInvokedWithValidId_thenExpectOk() {
        ItemRequestDto itemRequestDtoAdded = ItemRequestDto.builder().description("desc").created(LocalDateTime.now())
                .userId(3L).build();
        when(itemRequestService.addItemRequest(any(ItemRequestDto.class), anyLong())).thenReturn(itemRequestDtoAdded);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("desc").build();
        String response = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemRequestService, atLeast(1)).addItemRequest(any(ItemRequestDto.class), anyLong());
        assertEquals(response, objectMapper.writeValueAsString(itemRequestDtoAdded));
    }

    @Test
    @SneakyThrows
    public void createItemRequest_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(itemRequestService.addItemRequest(any(ItemRequestDto.class), anyLong())).thenThrow(NotFoundException.class);
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("desc").build();
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        verify(itemRequestService, atLeast(1)).addItemRequest(any(ItemRequestDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getItemRequests_whenInvokedWithValidUserId_thenExpectOk() {
        List<ItemRequestDto> itemRequests = List.of(ItemRequestDto.builder().description("desc")
                .created(LocalDateTime.now()).userId(3L).build());
        when(itemRequestService.getItemRequestsByUserId(anyLong())).thenReturn(itemRequests);
        String response = mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemRequestService, atLeast(1)).getItemRequestsByUserId(anyLong());
        assertEquals(response, objectMapper.writeValueAsString(itemRequests));
    }

    @Test
    @SneakyThrows
    public void getItemRequests_whenInvokedWithInvalidUserId_thenExpectNotFound() {
        when(itemRequestService.getItemRequestsByUserId(anyLong())).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        verify(itemRequestService, atLeast(1)).getItemRequestsByUserId(anyLong());
    }

    @Test
    @SneakyThrows
    public void getItemRequestsByOtherUsers_whenInvokedWithValidUserId_thenExpectOk() {
        List<ItemRequestDto> itemRequests = List.of(ItemRequestDto.builder().description("desc")
                .created(LocalDateTime.now()).userId(3L).build());
        when(itemRequestService.getItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt())).thenReturn(itemRequests);
        String response = mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 4L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemRequestService, atLeast(1))
                .getItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt());
        assertEquals(response, objectMapper.writeValueAsString(itemRequests));
    }

    @Test
    @SneakyThrows
    public void getItemRequestsByOtherUsers_whenInvokedWithInvalidUserId_thenExpectNotFound() {
        when(itemRequestService.getItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt()))
                .thenThrow(NotFoundException.class);
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 4L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse();
        verify(itemRequestService, atLeast(1))
                .getItemRequestsByOtherUsers(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getItemRequestById_whenInvokedWithValidIds_thenExpectOk() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().id(4L).description("desc")
                .created(LocalDateTime.now()).userId(3L).build();
        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);
        String response = mockMvc.perform(get("/requests/4")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemRequestService, atLeast(1)).getItemRequestById(anyLong(), anyLong());
        assertEquals(response, objectMapper.writeValueAsString(itemRequestDto));
    }

    @Test
    @SneakyThrows
    public void getItemRequestById_whenInvokedWithInvalidId_thenExpectNotFound() {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/requests/4")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemRequestService, atLeast(1)).getItemRequestById(anyLong(), anyLong());
    }
}
