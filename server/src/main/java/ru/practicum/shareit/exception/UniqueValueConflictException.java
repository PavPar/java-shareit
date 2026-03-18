package ru.practicum.shareit.exception;

public class UniqueValueConflictException extends RuntimeException {
    public UniqueValueConflictException() {
        super("Unique exception for value");
    }

    public UniqueValueConflictException(String message) {
        super(message);
    }
}
