package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("itemServiceInMemory")
public class ItemServiceInMemory implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceInMemory(@Qualifier("inMemoryItemStorage") ItemStorage itemStorage,
                               @Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;

    }

    public Collection<ItemDto> getAll(Long ownerId) {
        return itemStorage.getAll(ownerId)
                .stream().map(ItemMapper::toDto).collect(Collectors.toList());
    }


    public ItemDto getOne(Long ownerId, long id) {
        return ItemMapper.toDto(
                itemStorage.getOne(id).orElseThrow(() -> new NotFoundException("no item"))
        );
    }

    public ItemDto add(Long ownerId, ItemCreateDto dto) {
        Item item = ItemMapper.toEntity(dto);
        userStorage.getOne(ownerId).orElseThrow(() -> new NotFoundException("no user"));
        item.setOwnerId(ownerId);
        return ItemMapper.toDto(
                itemStorage.add(item)
        );
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        throw new InternalServerException("not implemented");
    }


    public ItemDto update(Long ownerId, long id, ItemUpdateDto dto) {
        userStorage.getOne(ownerId).orElseThrow(() -> new NotFoundException("no user"));
        Item currentItem = itemStorage.getOne(id).orElseThrow(() -> new NotFoundException("no item"));

        return ItemMapper.toDto(
                itemStorage.update(id, ItemMapper.mapToItem(dto, currentItem))
        );
    }

    public Collection<ItemDto> search(String query) {
        if (query.isBlank()) {
            return List.of();
        }
        return itemStorage.search(query)
                .stream().map(ItemMapper::toDto).collect(Collectors.toList());
    }
}
