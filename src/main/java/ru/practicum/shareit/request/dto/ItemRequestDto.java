package ru.practicum.shareit.request.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemRequestDto {

    Long id;

    @NotNull(message = "Описание не должно быть пустым.")
    @NotBlank(message = "Описание не должно быть пустым.")
    String description;

    Long userId;

    LocalDateTime created;
}
