package ru.practicum.shareit.user.model;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

public class UserMapper {

    public static UserEntity mapUserDtoToUserEntity(UserDto userDto) {
        return UserEntity.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserEntity mapUserDtoToUserEntity(UserDtoUpdate userDto) {
        return UserEntity.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserDto mapUserEntityToUserDto(UserEntity userEntity) {
        return UserDto.builder()
                .id(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .build();
    }
}
