package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class UserDto {
    long id;
    String name;
    String email;
}
