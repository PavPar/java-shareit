package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {
    UserService service;

    @GetMapping
    public Collection<UserDto> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public UserDto getOne(@PathVariable long id) {
        return service.getOne(id);
    }

    @PostMapping
    public UserDto add(@Valid @RequestBody UserDto user) {
        return service.add(user);
    }

    @PatchMapping("{id}")
    public UserDto update(@PathVariable long id, @Valid @RequestBody UserUpdateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        service.remove(id);
    }
}
