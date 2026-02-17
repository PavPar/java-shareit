package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequestMapping("/items")
public class ItemController {
    ItemService service;

    public ItemController(@Qualifier("itemServiceImpl") ItemService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader(value = Constants.HeaderUserIdField, required = false) Long ownerId) {
        return service.getAll(ownerId);
    }

    @GetMapping("{id}")
    public ItemDto getOne(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id) {
        return service.getOne(ownerId, id);
    }

    @GetMapping("search")
    public Collection<ItemDto> search(@RequestParam("text") String text) {
        return service.search(text);
    }

    @PostMapping
    public ItemDto add(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @Valid @RequestBody ItemCreateDto dto) {
        return service.add(ownerId, dto);
    }

    @PatchMapping("{id}")
    public ItemDto update(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @Valid @RequestBody ItemUpdateDto dto) {
        return service.update(ownerId, id, dto);
    }

    @PostMapping("{id}/comment")
    public CommentDto addComment(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @Valid @RequestBody CommentCreateDto dto) {
        return service.addComment(ownerId, id, dto);
    }
}
