package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {
    Optional<Item> getOne(long id);

    Collection<Item> getAll(long id);

    Collection<Item> search(String query);

    Item add(Item item);

    Item update(long id, Item item);
}
