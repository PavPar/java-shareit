package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Item {
    long id;
    long owner;
    String name;
    String description;
    boolean available;

//    ItemRequest request;
}
