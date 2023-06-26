package ru.practicum.shareit.exception;

public class Validation {

    public static void validatePagination(int from, int size) {
        if (from < 0) {
            throw new BadRequestException(String.format("Не верный параметр from=%s", from));
        }
        if (size < 1) {
            throw new BadRequestException(String.format("Не верный параметр size=%s", size));
        }
    }
}
