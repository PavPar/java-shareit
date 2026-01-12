package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage storage;

    public UserService(@Qualifier("inMemoryUserStorage") UserStorage storage) {
        this.storage = storage;
    }

    public UserDto add(UserDto dto) {
        Optional<User> addedUser = storage.add(UserMapper.toEntity(dto));
        if (addedUser.isEmpty()) {
            throw new InternalServerException("Failed to add user");
        }
        return UserMapper.toDto(addedUser.get());
    }

    public Collection<UserDto> getAll() {
        return storage.getAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getOne(long id) {
        return UserMapper.toDto(storage.getOne(id).orElseThrow(() -> new NotFoundException("no user")));
    }

    public UserDto update(long id, UserUpdateDto dto) {
        User updatedUser = storage.update(id, dto);
        return UserMapper.toDto(updatedUser);
    }

    public void remove(long id) {
        storage.deleteOne(id);
    }
}
