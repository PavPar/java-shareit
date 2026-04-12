package ru.practicum.shareit.user.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UniqueValueConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Qualifier("userServiceImpl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto add(UserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UniqueValueConflictException("email not unique");
        }

        User addedUser = userRepository.save(UserMapper.toEntity(dto));
        return UserMapper.toDto(addedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getOne(long id) {
        return UserMapper.toDto(userRepository.findById(id).orElseThrow(() -> new NotFoundException("no user")));
    }

    @Override
    public UserDto update(long id, UserUpdateDto dto) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException("no user"));

        if (userRepository.existsByEmail(dto.getEmail()) && !dto.getEmail().equals(existingUser.getEmail())) {
            throw new UniqueValueConflictException("email not unique");
        }

        if (!Objects.isNull(dto.getEmail())) {
            existingUser.setEmail(dto.getEmail());
        }

        if (!Objects.isNull(dto.getName())) {
            existingUser.setName(dto.getName());
        }
        return UserMapper.toDto(existingUser);
    }

    @Override
    public void remove(long id) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException("no user"));

        userRepository.delete(existingUser);
    }
}
