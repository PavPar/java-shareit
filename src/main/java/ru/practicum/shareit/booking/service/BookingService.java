package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto add(Long ownerId, BookingCreateDto dto);

    BookingDto changeApprovedStatusTo(Long ownerId, Long bookingId, Boolean approved);

    BookingDto getBooking(Long id, Long ownerId);

    List<BookingDto> getAllBookingsCreatedByUser(Long ownerId, BookingGetAllState state);

    List<BookingDto> getAllUserBookings(Long ownerId, BookingGetAllState state);
}
