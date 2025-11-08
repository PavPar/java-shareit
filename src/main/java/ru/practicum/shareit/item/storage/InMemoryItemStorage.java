package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> storage = new HashMap<>();

    @Override
    public Optional<Item> getOne(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Collection<Item> getAll(long id) {
        return storage.values().stream().filter(i -> i.getOwner() == id).collect(Collectors.toList());
    }

    @Override
    public Item add(Item item) {
        item.setId(getNextId());
        storage.put(item.getId(), item);
        return storage.get(item.getId());
    }

    @Override
    public Item update(long id, Item item) {
        if (!storage.containsKey(id)) {
            throw new NotFoundException("not found item");
        }
        storage.put(id, item);
        return storage.get(id);
    }

    @Override
    public Collection<Item> search(String query) {

        return storage.values().stream()
                .filter(i -> i.isAvailable()
                                && (i.getDescription().toLowerCase().contains(query.toLowerCase())
                                || i.getName().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
