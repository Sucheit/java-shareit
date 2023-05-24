package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.UserEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDtoUpdate {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private UserEntity userEntity;
}
