package ru.practicum.shareit.booking;

import org.junit.Before;
import org.junit.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class BookingDtoValidationTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testItemIdIsNull() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testStartIsNull() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(6L)
                .start(null)
                .end(LocalDateTime.now().plusHours(2))
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testStartIsNotInFuture() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(6L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEndIsNull() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(6L)
                .end(null)
                .start(LocalDateTime.now().plusHours(2))
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEndIsNotInFuture() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(6L)
                .end(LocalDateTime.now().minusHours(1))
                .start(LocalDateTime.now().plusHours(2))
                .build();
        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);
        assertFalse(violations.isEmpty());
    }
}
