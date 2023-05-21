package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

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
    public UserDto updateUser(@PathVariable Long id, @RequestBody @Valid UserDtoUpdate userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        log.info("Обновили пользователя: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping(value = "/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        log.info("Получили пользователя: {}", user);
        return user;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteUserById(@PathVariable Long id) {
        log.info("Удалили пользователя id={}", id);
        userService.deleteUserById(id);
    }
}
