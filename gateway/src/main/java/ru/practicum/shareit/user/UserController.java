package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient client;


    @GetMapping
    public ResponseEntity<Object> getAll() {
        return client.getAll();
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getOne(@PathVariable long id) {
        return client.getOne(id);
    }

    @PostMapping
    public ResponseEntity<Object> add(@Valid @RequestBody UserDto user) {
        return client.add(user);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> update(@PathVariable long id, @Valid @RequestBody UserUpdateDto dto) {
        return client.update(id, dto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        return client.remove(id);
    }
}
