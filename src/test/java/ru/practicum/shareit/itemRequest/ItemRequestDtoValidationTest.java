package ru.practicum.shareit.itemRequest;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDtoValidationTest {

    Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testDescriptionIsNull() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().build();
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDescriptionIsBlank() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("").build();
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);
        assertFalse(violations.isEmpty());
    }

}
