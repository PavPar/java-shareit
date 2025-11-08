package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Objects;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public static Item toEntity(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.isAvailable())
                .build();
    }

    public static Item toEntity(ItemUpdateDto dto) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.isAvailable())
                .build();
    }

    public static Item toEntity(ItemCreateDto dto) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();
    }

    public static Item mapToItem(ItemUpdateDto dto, Item item){
        Item.ItemBuilder builder = item.toBuilder();
        if(!Objects.isNull(dto.getName())){
            builder.name(dto.getName());
        }
        if(!Objects.isNull(dto.getDescription())){
            builder.description(dto.getDescription());
        }
        if(!Objects.isNull(dto.isAvailable())){
            builder.available(dto.isAvailable());
        }
        return builder.build();
    }
}
