package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Qualifier;
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
public class UserController {
    UserService service;

    public UserController(@Qualifier("userServiceImpl") UserService service) {
        this.service = service;
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public UserDto getOne(@PathVariable long id) {
        return service.getOne(id);
    }

    @PostMapping
    public UserDto add(@RequestBody UserDto user) {
        return service.add(user);
    }

    @PatchMapping("{id}")
    public UserDto update(@PathVariable long id, @RequestBody UserUpdateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable long id) {
        service.remove(id);
    }
}
