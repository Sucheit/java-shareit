package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.user.model.UserMapper.mapUserDtoToUser;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    @Test
    public void getUserById_whenValidId_thenGettingUser() {
        User user = User.builder().id(1L).email("name@mail.com").name("name").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(1L);

        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    public void getUserById_whenInvalidId_thenExpectingNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(anyLong()));
    }

    @Test
    public void getUsers_expectingSuccess() {
        List<User> returnedUsers = List.of(User.builder().id(77L).email("name@mail.com").name("name").build());
        when(userRepository.findAll()).thenReturn(returnedUsers);

        List<UserDto> users = userService.getUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(77, users.get(0).getId());
    }

    @Test
    public void deleteUserById_givenValidId_expectSuccess() {
        User user = User.builder().id(1L).email("name@mail.com").name("name").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        doNothing().when(userRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> userService.deleteUserById(1L));
    }

    @Test
    public void deleteUserById_givenInvalidId_expectNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUserById(anyLong()));
    }

    @Test
    public void addUser_expectSuccess() {
        User user = User.builder().id(1L).email("name@mail.com").name("name").build();
        UserDto userDto = UserDto.builder().id(null).email("name@mail.com").name("name").build();
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        UserDto addUser = userService.addUser(userDto);

        assertNotNull(addUser);
        assertEquals(1, addUser.getId());
        assertEquals("name", addUser.getName());
    }

    @Test
    public void updateUser_givenValidData_expectSuccess() {
        User user = User.builder().id(1L).email("name@mail.com").name("name").build();
        UserDtoUpdate userDto = UserDtoUpdate.builder().id(1L).email("newName@mail.com").name("newName").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(mapUserDtoToUser(userDto))).thenReturn(mapUserDtoToUser(userDto));
        when(userRepository.existsByEmail("newName@mail.com")).thenReturn(Boolean.FALSE);

        userService.updateUser(1L, userDto);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(1, savedUser.getId());
        assertEquals("newName", savedUser.getName());
        assertEquals("newName@mail.com", savedUser.getEmail());
    }

    @Test
    public void updateUser_givenExistedEmail_expectAlreadyExistsException() {
        User user = User.builder().id(1L).email("name@mail.com").name("name").build();
        UserDtoUpdate userDto = UserDtoUpdate.builder().id(1L).email("newName@mail.com").name("newName").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newName@mail.com")).thenReturn(Boolean.TRUE);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, userDto));
    }

    @Test
    public void updateUser_givenEmailIsNull_expectSuccess() {
        User user = User.builder().id(1L).email("name@mail.com").name("newName").build();
        UserDtoUpdate userDto = UserDtoUpdate.builder().id(1L).email(null).name("newName").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUser(1L, userDto);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(1, savedUser.getId());
        assertEquals("newName", savedUser.getName());
        assertEquals("name@mail.com", savedUser.getEmail());
    }

    @Test
    public void updateUser_givenNameIsNull_expectSuccess() {
        User user = User.builder().id(1L).email("newName@mail.com").name("name").build();
        UserDtoUpdate userDto = UserDtoUpdate.builder().id(1L).email("newName@mail.com").name(null).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userRepository.existsByEmail("newName@mail.com")).thenReturn(Boolean.FALSE);

        userService.updateUser(1L, userDto);

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(1, savedUser.getId());
        assertEquals("name", savedUser.getName());
        assertEquals("newName@mail.com", savedUser.getEmail());
    }

    @Test
    public void updateUser_givenInvalidId_expectNotFound() {
        UserDtoUpdate userDtoUpdate = UserDtoUpdate.builder().id(1L).email("name@mail.com").name("name").build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, userDtoUpdate));
    }
}
