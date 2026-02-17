package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

public interface ItemService {
    Collection<ItemDto> getAll(Long ownerId);

    ItemDto getOne(Long ownerId, long id);

    ItemDto add(Long ownerId, ItemCreateDto dto);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto);

    ItemDto update(Long ownerId, long id, ItemUpdateDto dto);

    Collection<ItemDto> search(String query);
}
