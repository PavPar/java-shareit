package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

public class UserMapper {
    public static UserDto toDto(User entity) {
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .build();
    }

    public static User toEntity(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static User mapTo(UserUpdateDto from, User to) {
        User.UserBuilder builder = to.toBuilder();
        if (!Objects.isNull(from.getName())) {
            builder.name(from.getName());
        }
        if (!Objects.isNull(from.getEmail())) {
            builder.email(from.getEmail());
        }
        return builder.build();
    }
}
