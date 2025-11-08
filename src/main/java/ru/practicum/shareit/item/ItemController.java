package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.websocket.server.PathParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    ItemService service;

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return service.getAll(ownerId);
    }

    @GetMapping("{id}")
    public ItemDto getOne(@PathVariable long id) {
        return service.getOne(id);
    }

    @GetMapping("search")
    public Collection<ItemDto> search(@RequestParam("text") String text) {
        return service.search(text);
    }

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemCreateDto dto) {
        return service.add(ownerId,dto);
    }

    @PatchMapping("{id}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long ownerId,@PathVariable long id, @Valid @RequestBody ItemUpdateDto dto) {
        return service.update(ownerId,id,dto);
    }
}
