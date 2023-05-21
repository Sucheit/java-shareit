package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void getAllUsers_ReturnOkWithListOfUsers() {
        List<UserDto> users = List.of(UserDto.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build()
        );
        when(userService.getUsers()).thenReturn(users);
        String response = mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).getUsers();
        assertEquals(response, objectMapper.writeValueAsString(users));
    }

    @SneakyThrows
    @Test
    public void getUser_whenInvokedWithValidId_thenReturnOkWithUser() {
        UserDto user = UserDto.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.getUserById(1L)).thenReturn(user);
        String response = mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).getUserById(1L);
        assertEquals(response, objectMapper.writeValueAsString(user));
    }

    @SneakyThrows
    @Test
    public void getUser_whenInvokedWithInvalidId_thenReturnNotFound() {
        when(userService.getUserById(99L)).thenThrow(NotFoundException.class);
        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).getUserById(99L);
    }

    @SneakyThrows
    @Test
    public void deleteUser_whenInvokedWithValidId_thenReturnOk() {
        doNothing().when(userService).deleteUserById(99L);
        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).deleteUserById(99L);
    }

    @SneakyThrows
    @Test
    public void deleteUser_whenInvokedWithInvalidId_thenReturnNotFound() {
        doThrow(NotFoundException.class).when(userService).deleteUserById(99L);
        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).deleteUserById(99L);
    }

    @SneakyThrows
    @Test
    public void addUser_whenInvokedWithValidUser_thenReturnOk() {
        UserDto user = UserDto.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.addUser(user)).thenReturn(user);
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).addUser(user);
        Assertions.assertEquals(response, objectMapper.writeValueAsString(user));
    }

    @SneakyThrows
    @Test
    public void addUser_whenInvokedWithInvalidEmail_thenReturnConflict() {
        UserDto user = UserDto.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.addUser(user)).thenThrow(AlreadyExistsException.class);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).addUser(user);
    }

    @SneakyThrows
    @Test
    public void updateUser_whenInvokedWithValidUser_thenReturnOk() {
        UserDtoUpdate user = UserDtoUpdate.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.updateUser(user.getId(), user)).thenReturn(updatedUser);
        String response = mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).updateUser(user.getId(), user);
        Assertions.assertEquals(response, objectMapper.writeValueAsString(updatedUser));
    }

    @SneakyThrows
    @Test
    public void updateUser_whenInvokedWithInvalidUserId_thenReturnNotFound() {
        UserDtoUpdate user = UserDtoUpdate.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.updateUser(user.getId(), user)).thenThrow(NotFoundException.class);
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).updateUser(user.getId(), user);
    }

    @SneakyThrows
    @Test
    public void updateUser_whenInvokedWithInvalidEmail_thenReturnNotFound() {
        UserDtoUpdate user = UserDtoUpdate.builder()
                .id(1L)
                .email("name1@mail.com")
                .name("name1")
                .build();
        when(userService.updateUser(user.getId(), user)).thenThrow(AlreadyExistsException.class);
        mockMvc.perform(MockMvcRequestBuilders.patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userService, atLeast(1)).updateUser(user.getId(), user);
    }
}
