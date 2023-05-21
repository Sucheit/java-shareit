package ru.practicum.shareit.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.model.UserEntity;

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
        Optional<UserEntity> optionalUser = Optional.of(UserEntity.builder()
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
        List<UserEntity> returnedUsers = List.of(UserEntity.builder()
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
        Optional<UserEntity> optionalUser = Optional.of(UserEntity.builder()
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
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        UserDto userDto = UserDto.builder()
                .id(null)
                .email("name@mail.com")
                .name("name")
                .build();
        when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(userEntity);
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(Boolean.FALSE);
        UserDto addUser = userService.addUser(userDto);
        assertNotNull(addUser);
        assertEquals(1, addUser.getId());
        assertEquals("name", addUser.getName());
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(Boolean.TRUE);
        assertThrows(AlreadyExistsException.class, () -> userService.addUser(userDto));
    }

    @Test
    public void updateUser() {
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        UserDtoUpdate userDtoUpdate = UserDtoUpdate.builder()
                .id(1L)
                .email("name@mail.com")
                .name("name")
                .build();
        when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(userEntity);
        when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(Boolean.FALSE);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(userEntity));
        UserDto addUser = userService.updateUser(1L, userDtoUpdate);
        assertNotNull(addUser);
        assertEquals(1, addUser.getId());
        assertEquals("name", addUser.getName());
        assertEquals("name@mail.com", addUser.getEmail());
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, userDtoUpdate));
    }
}
