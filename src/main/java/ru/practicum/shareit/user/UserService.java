package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.model.UserMapper.mapUserDtoToUserEntity;
import static ru.practicum.shareit.user.model.UserMapper.mapUserEntityToUserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id='%s' не найден", id)));
        return mapUserEntityToUserDto(user);
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        User user = mapUserDtoToUserEntity(userDto);
        return mapUserEntityToUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDtoUpdate userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь id='%s' не найден", id)));
        if (userRepository.existsByEmail(userDto.getEmail())
                && !user.getEmail().equals(userDto.getEmail())) {
            throw new AlreadyExistsException(
                    String.format("Пользователь с email: '%s' уже существует", userDto.getEmail()));
        }
        userDto.setId(id);
        User userToUpdate = UserMapper.mapUserDtoToUserEntity(userDto);
        if (userToUpdate.getName() == null) {
            userToUpdate.setName(user.getName());
        }
        if (userToUpdate.getEmail() == null) {
            userToUpdate.setEmail(user.getEmail());
        }
        return mapUserEntityToUserDto(userRepository.save(userToUpdate));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapUserEntityToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUserById(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", id));
        }
        userRepository.deleteById(id);
    }
}
