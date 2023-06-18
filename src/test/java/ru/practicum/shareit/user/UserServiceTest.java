package ru.practicum.shareit.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void getUserById() {
        Optional<User> optionalUser = Optional.of(User.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(optionalUser);
        UserDto user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals(user.getId(), optionalUser.get().getId());
        assertEquals(user.getName(), optionalUser.get().getName());
        assertEquals(user.getEmail(), optionalUser.get().getEmail());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    public void getUsers() {
        List<User> returnedUsers = List.of(User.builder()
                .id(77L)
                .email("name@mail.com")
                .name("name")
                .build()
        );
        when(userRepository.findAll()).thenReturn(returnedUsers);
        List<UserDto> users = userService.getUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(77, users.get(0).getId());
    }

    @Test
    public void deleteUserById() {
        Optional<User> optionalUser = Optional.of(User.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(optionalUser);
        doNothing().when(userRepository).deleteById(Mockito.anyLong());
        assertDoesNotThrow(() -> userService.deleteUserById(1L));
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.deleteUserById(1L));
    }

    @Test
    public void addUser() {
        User user = User.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        UserDto userDto = UserDto.builder()
                .id(null)
                .email("name@mail.com")
                .name("name")
                .build();
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        UserDto addUser = userService.addUser(userDto);
        assertNotNull(addUser);
        assertEquals(1, addUser.getId());
        assertEquals("name", addUser.getName());
    }

    @Test
    public void updateUser() {
        User user = User.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        UserDtoUpdate userDtoUpdate = UserDtoUpdate.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(Boolean.FALSE);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        UserDto addUser = userService.updateUser(1L, userDtoUpdate);
        assertNotNull(addUser);
        assertEquals(1, addUser.getId());
        assertEquals("name", addUser.getName());
        assertEquals("name@mail.com", addUser.getEmail());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, userDtoUpdate));
    }
}
