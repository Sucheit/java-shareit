package ru.practicum.shareit.item;

import org.junit.Before;
import org.junit.Test;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ItemDtoValidationTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameIsNull() {
        ItemDto item = ItemDto.builder().id(1L).name(null).description("desc").available(Boolean.TRUE).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testNameIsEmpty() {
        ItemDto item = ItemDto.builder().id(1L).name("").description("desc").available(Boolean.TRUE).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDescriptionIsNull() {
        ItemDto item = ItemDto.builder().id(1L).name("name").description(null).available(Boolean.TRUE).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testDescriptionIsEmpty() {
        ItemDto item = ItemDto.builder().id(1L).name("name").description("").available(Boolean.TRUE).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testAvailableIsNull() {
        ItemDto item = ItemDto.builder().id(1L).name("name").description("desc").available(null).build();
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(item);
        assertFalse(violations.isEmpty());
    }
}
