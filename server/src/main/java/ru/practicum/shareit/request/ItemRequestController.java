package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }


    @PostMapping()
    public ItemRequestDto create(@RequestHeader(Constants.HeaderUserIdField) Long userId, @RequestBody ItemRequestCreateDto dto) {
        return service.save(userId, dto);
    }

    @GetMapping()
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(Constants.HeaderUserIdField) Long ownerId) {
        return service.getAllFromUser(ownerId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public ItemRequestDto getById(@PathVariable long id) {
        return service.getById(id);
    }
}
