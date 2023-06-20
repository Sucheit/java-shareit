package ru.practicum.shareit.request.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    Long id;

    @NotNull(message = "Описание не должно быть пустым.")
    @NotBlank(message = "Описание не должно быть пустым.")
    String description;

    Long userId;

    LocalDateTime created;

    List<ItemDto> items;
}
