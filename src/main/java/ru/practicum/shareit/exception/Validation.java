package ru.practicum.shareit.exception;

import ru.practicum.shareit.booking.model.State;

public class Validation {

    public static void validatePagination(int from, int size) {
        if (from < 0) {
            throw new BadRequestException(String.format("Не верный параметр from=%s", from));
        }
        if (size < 1) {
            throw new BadRequestException(String.format("Не верный параметр size=%s", size));
        }
    }

    public static State validateBookingState(String str) {
        State state;
        try {
            state = State.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("Unknown state: %s", str));
        }
        return state;
    }
}
