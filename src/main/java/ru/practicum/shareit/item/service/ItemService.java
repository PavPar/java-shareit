package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemService(@Qualifier("inMemoryItemStorage") ItemStorage itemStorage,
                       @Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;

    }

    public Collection<ItemDto> getAll(long ownerId) {
        return itemStorage.getAll(ownerId)
                .stream().map(ItemMapper::toDto).collect(Collectors.toList());
    }

    public ItemDto getOne(long id) {
        return ItemMapper.toDto(
                itemStorage.getOne(id).orElseThrow(() -> new NotFoundException("no item"))
        );
    }

    public ItemDto add(Long ownerId, ItemCreateDto dto) {
        Item item = ItemMapper.toEntity(dto);
        userStorage.getOne(ownerId).orElseThrow(() -> new NotFoundException("no user"));
        item.setOwner(ownerId);
        return ItemMapper.toDto(
                itemStorage.add(item)
        );
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
