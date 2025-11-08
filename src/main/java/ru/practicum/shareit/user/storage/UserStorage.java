package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    public Collection<User> getAll();
    public Optional<User> getOne(long id);
    public void deleteOne(long id);
    public User update(long id, UserUpdateDto user);
    public Optional<User> add(User user);
}
