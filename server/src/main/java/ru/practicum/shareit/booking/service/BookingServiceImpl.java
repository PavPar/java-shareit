package ru.practicum.shareit.booking.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.constants.BookingGetAllState;
import ru.practicum.shareit.booking.constants.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Qualifier("bookingServiceImpl")
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto add(Long ownerId, BookingCreateDto dto) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("no user"));
        Item item = itemRepository.findById(dto.getItemId()).orElseThrow(() -> new NotFoundException("no item"));
        if (!item.isAvailable()) {
            throw new ValidationException("item is not available");
        }
        if (item.getOwnerId() == user.getId()) {
            throw new ValidationException("cant book own item");
        }
        Booking booking = BookingMapper.toEntity(dto, item, user);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toDto(repository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto changeApprovedStatusTo(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = repository.findByIdWithItem(bookingId).orElseThrow(() -> new NotFoundException("no booking"));
        Item item = booking.getItem();

        if (!item.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("not owner");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBooking(Long id, Long ownerId) {
        Booking booking = repository.findById(id).orElseThrow(() -> new NotFoundException("no booking"));
        if (!Objects.equals(booking.getItem().getOwnerId(), ownerId) && !Objects.equals(booking.getBooker().getId(), ownerId)) {
            throw new ForbiddenException("Not an item or booking owner");
        }
        return BookingMapper.toDto(booking);
    }

    // Все которые создал пользователь
    @Override
    public List<BookingDto> getAllUserBookings(Long bookerId, BookingGetAllState state) {
        List<Booking> bookings;

        switch (state) {
            case null -> bookings = repository.findAllByBookerId(bookerId);
            case ALL -> bookings = repository.findAllByBookerId(bookerId);
            case PAST -> bookings = repository.findAllPastByBookerId(bookerId);
            case CURRENT -> bookings = repository.findAllCurrentByBookerId(bookerId);
            case FUTURE -> bookings = repository.findAllFutureByBookerId(bookerId);
            case REJECTED -> bookings = repository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED);
            case WAITING -> bookings = repository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING);
        }

        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    // Те заявки которые используют предмет пользователя
    @Override
    public List<BookingDto> getAllBookingsCreatedByUser(Long ownerId, BookingGetAllState state) {
        List<Booking> bookings;
        userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("no user"));

        switch (state) {
            case null -> bookings = repository.findAllByItemOwnerId(ownerId);
            case ALL -> bookings = repository.findAllByItemOwnerId(ownerId);
            case PAST -> bookings = repository.findPastByItemOwnerId(ownerId);
            case CURRENT -> bookings = repository.findCurrentByItemOwnerId(ownerId);
            case FUTURE -> bookings = repository.findFutureByItemOwnerId(ownerId);
            case REJECTED -> bookings = repository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED);
            case WAITING -> bookings = repository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING);
        }

        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}
