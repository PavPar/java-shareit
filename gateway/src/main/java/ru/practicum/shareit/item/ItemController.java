package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient client;

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(value = Constants.HeaderUserIdField, required = false) Long ownerId) {
        return client.getAll(ownerId);
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getOne(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id) {
        return client.getOne(ownerId, id);
    }

    @GetMapping("search")
    public ResponseEntity<Object> search(@RequestParam("text") String text) {
        return client.search(text);
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @Valid @RequestBody ItemCreateDto dto) {
        return client.add(ownerId, dto);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> update(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @RequestBody ItemUpdateDto dto) {
        return client.update(ownerId, id, dto);
    }

    @PostMapping("{id}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @RequestBody CommentCreateDto dto) {
        return client.addComment(ownerId, id, dto);
    }
}
