package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    long id;
    String description;
    UserDto requester;
    LocalDateTime created;
    List<ItemDto> items;
}
