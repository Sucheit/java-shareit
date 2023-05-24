package ru.practicum.shareit.user;

import org.junit.Before;
import org.junit.Test;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserDtoValidationTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameIsNull() {
        UserDto user = UserDto.builder().id(1L).name(null).email("email@mail.com").build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmailIsNull() {
        UserDto user = UserDto.builder().id(1L).name("name").email(null).build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidEmail() {
        UserDto user = UserDto.builder().id(1L).name("name").email("invalid.email").build();
        Set<ConstraintViolation<UserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }
}
