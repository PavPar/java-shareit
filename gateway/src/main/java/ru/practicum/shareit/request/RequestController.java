package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestClient client;

    @PostMapping()
    public ResponseEntity<Object> create(@RequestHeader(Constants.HeaderUserIdField) Long userId, @RequestBody ItemRequestCreateDto dto) {
        return client.save(userId, dto);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllUserRequests(@RequestHeader(Constants.HeaderUserIdField) Long ownerId) {
        return client.getAllFromUser(ownerId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll() {
        return client.getAll();
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getById(@PathVariable long id) {
        return client.getById(id);
    }
}
