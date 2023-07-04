package ru.practicum.shareit.user;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {

    UserClient userClient;

    @PostMapping()
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Creating user: {}", userDto);
        return userClient.createUser(userDto);
    }

    @GetMapping()
    public ResponseEntity<Object> getUsers() {
        log.info("Getting users");
        return userClient.getUsers();
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable long id,
                                             @RequestBody @Valid UserDtoUpdate userDto) {
        log.info("Updating users id={}, dto: {}", id, userDto);
        return userClient.updateUser(id, userDto);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable long id) {
        log.info("Getting user id={}", id);
        return userClient.getUserById(id);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable long id) {
        log.info("Deleting user id={}", id);
        return userClient.deleteUserById(id);
    }
}
