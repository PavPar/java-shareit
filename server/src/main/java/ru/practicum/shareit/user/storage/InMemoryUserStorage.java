package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UniqueValueConflictException;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    final Map<Long, User> userStorage = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return userStorage.values();
    }

    @Override
    public Optional<User> getOne(long id) {
        return Optional.ofNullable(userStorage.get(id));
    }

    @Override
    public void deleteOne(long id) {
        if (!userStorage.containsKey(id)) {
            throw new NotFoundException("no user for " + id);
        }
        ;
        userStorage.remove(id);
    }

    @Override
    public User update(long id, UserUpdateDto user) {
        if (!isEmailUnique(user.getEmail())) {
            throw new UniqueValueConflictException("email not unique");
        }

        User currentUser = Optional.ofNullable(userStorage.get(id))
                .orElseThrow(() -> new NotFoundException("no user for " + id));
        User userForUpdate = UserMapper.mapTo(user, currentUser);
        userForUpdate.setId(id);
        userStorage.put(id, userForUpdate);
        return userStorage.get(id);
    }

    @Override
    public Optional<User> add(User user) {
        if (!isEmailUnique(user.getEmail())) {
            throw new UniqueValueConflictException("email not unique");
        }
        long id = getNextId();
        user.setId(id);
        userStorage.put(user.getId(), user);
        return Optional.of(user);
    }

    private boolean isEmailUnique(String email) {
        return this.userStorage.values().stream()
                .filter(u -> u.getEmail()
                        .equals(email))
                .findFirst()
                .isEmpty();
    }

    private long getNextId() {
        long currentMaxId = userStorage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
