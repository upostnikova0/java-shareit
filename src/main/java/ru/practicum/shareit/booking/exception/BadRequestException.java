package ru.practicum.shareit.booking.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String state) {
        super(state);
    }
}
