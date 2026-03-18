package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.app.Constants;
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    BookingService service;

    public BookingController(@Qualifier("bookingServiceImpl") BookingService service) {
        this.service = service;
    }


    @GetMapping("{id}")
    public BookingDto getOne(@PathVariable long id, @RequestHeader(Constants.HeaderUserIdField) Long ownerId) {
        return service.getBooking(id, ownerId);
    }

    @GetMapping
    public List<BookingDto> getAll(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @RequestParam(required = false) BookingGetAllState state) {
        return service.getAllUserBookings(ownerId, state);
    }

    @GetMapping("owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @RequestParam(required = false) BookingGetAllState state) {
        return service.getAllBookingsCreatedByUser(ownerId, state);
    }


    @PostMapping
    public BookingDto add(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @RequestBody BookingCreateDto dto) {
        return service.add(ownerId, dto);
    }

    @PatchMapping("{id}")
    public BookingDto changeApprove(@RequestHeader(Constants.HeaderUserIdField) Long ownerId, @PathVariable long id, @RequestParam Boolean approved) {
        return service.changeApprovedStatusTo(ownerId, id, approved);
    }
}
