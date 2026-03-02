package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Collection;

public interface UserService {
    UserDto add(UserDto dto);

    Collection<UserDto> getAll();

    UserDto getOne(long id);

    UserDto update(long id, UserUpdateDto dto);

    void remove(long id);
}
