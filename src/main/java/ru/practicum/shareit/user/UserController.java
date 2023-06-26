package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping()
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        UserDto addedUser = userService.addUser(userDto);
        log.info("Добавили пользователя: {}", addedUser);
        return addedUser;
    }

    @GetMapping()
    public List<UserDto> getUsers() {
        List<UserDto> users = userService.getUsers();
        log.info("Получили список пользователей: {}", users.size());
        return userService.getUsers();
    }

    @PatchMapping(value = "/{id}")
    public UserDto updateUser(@PathVariable long id, @RequestBody @Valid UserDtoUpdate userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        log.info("Обновили пользователя: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping(value = "/{id}")
    public UserDto getUserById(@PathVariable long id) {
        UserDto user = userService.getUserById(id);
        log.info("Получили пользователя: {}", user);
        return user;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.info("Удалили пользователя id={}", id);
        userService.deleteUserById(id);
    }
}
