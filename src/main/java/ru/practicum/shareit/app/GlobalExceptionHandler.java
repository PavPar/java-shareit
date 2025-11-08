package ru.practicum.shareit.app;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UniqueValueConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.InternalServerException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        return Map.of(HttpStatus.NOT_FOUND.toString(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationFail(final ValidationException e) {
        return Map.of(HttpStatus.BAD_REQUEST.toString(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalError(final InternalServerException e) {
        return Map.of(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleUniqueConflict(final UniqueValueConflictException e) {
        return Map.of(HttpStatus.CONFLICT.toString(), e.getMessage());
    }
}