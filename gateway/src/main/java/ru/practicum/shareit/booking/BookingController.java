package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;


@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping("{id}")
    public ResponseEntity<Object> getOne(@PathVariable long id, @RequestHeader(Constants.HeaderUserIdField) Long ownerId) {
        return bookingClient.getBooking(ownerId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @RequestParam(required = false) BookingGetAllState state) {
        return bookingClient.getAll(ownerId, state);
    }

    @GetMapping("owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @RequestParam(required = false) BookingGetAllState state) {
        return bookingClient.getAllByOwner(ownerId, state);
    }

    @PostMapping
    public ResponseEntity<Object> add(
            @RequestHeader(Constants.HeaderUserIdField) Long ownerId,
            @Valid @RequestBody BookingCreateDto dto) {
        return bookingClient.add(ownerId, dto);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> changeApprove(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @RequestParam Boolean approved) {
        return bookingClient.changeApprovedStatusTo(ownerId, id, approved);
    }
}
