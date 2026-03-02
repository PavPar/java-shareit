package ru.practicum.shareit.exception;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Not found exception");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
