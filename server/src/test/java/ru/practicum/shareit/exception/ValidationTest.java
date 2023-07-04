package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.exception.Validation.validatePagination;

class ValidationTest {

    @Test
    void validatePagination_giveInvalidFrom_expectBadRequest() {
        int from = -1;
        int size = 10;

        assertThrows(BadRequestException.class, () -> validatePagination(from, size));
    }

    @Test
    void validatePagination_giveInvalidSize_expectBadRequest() {
        int from = 3;
        int size = 0;

        assertThrows(BadRequestException.class, () -> validatePagination(from, size));
    }
}