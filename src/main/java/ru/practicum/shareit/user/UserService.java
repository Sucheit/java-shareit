package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.model.UserEntity;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.model.UserMapper.mapUserDtoToUserEntity;
import static ru.practicum.shareit.user.model.UserMapper.mapUserEntityToUserDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto getUserById(Long id) {
        Optional<UserEntity> userEntity = userRepository.findById(id);
        if (userEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", id));
        }
        return mapUserEntityToUserDto(userEntity.get());
    }

    public UserDto addUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new AlreadyExistsException(
                    String.format("Пользователь с email: '%s' уже существует", userDto.getEmail()));
        }
        UserEntity userEntity = mapUserDtoToUserEntity(userDto);
        return mapUserEntityToUserDto(userRepository.save(userEntity));
    }

    public UserDto updateUser(Long id, UserDtoUpdate userDto) {
        Optional<UserEntity> userEntity = userRepository.findById(id);
        if (userEntity.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", id));
        }
        if (userRepository.existsByEmail(userDto.getEmail())
                && !userEntity.get().getEmail().equals(userDto.getEmail())) {
            throw new AlreadyExistsException(
                    String.format("Пользователь с email: '%s' уже существует", userDto.getEmail()));
        }
        userDto.setId(id);
        UserEntity userToUpdate = UserMapper.mapUserDtoToUserEntity(userDto);
        if (userToUpdate.getName() == null) {
            userToUpdate.setName(userEntity.get().getName());
        }
        if (userToUpdate.getEmail() == null) {
            userToUpdate.setEmail(userEntity.get().getEmail());
        }
        return mapUserEntityToUserDto(userRepository.save(userToUpdate));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapUserEntityToUserDto)
                .collect(Collectors.toList());
    }

    public void deleteUserById(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь id='%s' не найден", id));
        }
        userRepository.deleteById(id);
    }
}
