package ru.practicum.shareit.item;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void addItem_whenInvokedWithValidUser_thenExpectOk() {
        ItemDto item = ItemDto.builder().id(3L).name("name").description("desc").available(Boolean.TRUE).build();
        when(itemService.addItem(1L, item)).thenReturn(item);
        String response = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).addItem(1L, item);
        assertEquals(response, objectMapper.writeValueAsString(item));
    }

    @Test
    @SneakyThrows
    public void addItem_whenInvokedWithInvalidUser_thenExpectNotFound() {
        ItemDto item = ItemDto.builder().id(3L).name("name").description("desc").available(Boolean.TRUE).build();
        when(itemService.addItem(1L, item)).thenThrow(NotFoundException.class);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).addItem(1L, item);
    }

    @Test
    @SneakyThrows
    public void updateItem_whenInvokedWithValidUser_thenExpectOk() {
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        ItemDto item = ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        when(itemService.updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId())).thenReturn(item);
        String response = mockMvc.perform(patch("/items/3")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId());
        assertEquals(response, objectMapper.writeValueAsString(item));
    }

    @Test
    @SneakyThrows
    public void updateItem_whenInvokedWithInvalidItemIdOrUserId_thenExpectNotFound() {
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        ItemDto item = ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        when(itemService.updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId())).thenThrow(NotFoundException.class);
        mockMvc.perform(patch("/items/3")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId());
    }

    @Test
    @SneakyThrows
    public void updateItem_whenInvokedWithConflictItemAndUser_thenExpectForbidden() {
        ItemDtoUpdate itemDtoUpdate = ItemDtoUpdate.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        ItemDto item = ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        when(itemService.updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId())).thenThrow(ForbiddenException.class);
        mockMvc.perform(patch("/items/3")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).updateItem(1L, itemDtoUpdate, itemDtoUpdate.getId());
    }

    @Test
    @SneakyThrows
    public void getItemById_whenInvokedWithValidItemId_thenExpectOk() {
        ItemDto item = ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build();
        when(itemService.getItemById(Mockito.anyLong())).thenReturn(item);
        String response = mockMvc.perform(get("/items/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).getItemById(Mockito.anyLong());
        assertEquals(response, objectMapper.writeValueAsString(item));
    }

    @Test
    @SneakyThrows
    public void getItemById_whenInvokedWithInvalidItemId_thenExpectNotFound() {
        when(itemService.getItemById(Mockito.anyLong())).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/items/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).getItemById(Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    public void getItemsByUserId_whenInvokedWithValidUserId_thenExpectOk() {
        List<ItemDto> items = List.of(ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build());
        when(itemService.findAllByUserId(Mockito.anyLong())).thenReturn(items);
        String response = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", Mockito.anyLong())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).findAllByUserId(Mockito.anyLong());
        assertEquals(response, objectMapper.writeValueAsString(items));
    }

    @Test
    @SneakyThrows
    public void getItemsByUserId_whenInvokedWithInvalidUserId_thenExpectNotFound() {
        when(itemService.findAllByUserId(Mockito.anyLong())).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", Mockito.anyLong())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).findAllByUserId(Mockito.anyLong());
    }

    @Test
    @SneakyThrows
    public void findBySearchTerm_thenExpectOk() {
        List<ItemDto> items = List.of(ItemDto.builder().id(3L).name("name")
                .description("desc").available(Boolean.TRUE).build());
        when(itemService.findByNameOrDescription(Mockito.anyString())).thenReturn(items);
        String response = mockMvc.perform(get("/items/search?text=searchTerm")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(itemService, atLeast(1)).findByNameOrDescription(Mockito.anyString());
        assertEquals(response, objectMapper.writeValueAsString(items));
    }
}
