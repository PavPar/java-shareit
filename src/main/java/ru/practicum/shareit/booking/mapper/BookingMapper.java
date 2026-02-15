package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static Booking toEntity(BookingCreateDto dto, Item item, User user) {
        return Booking.builder()
                .item(item)
                .booker(user)
                .end(dto.getEnd())
                .start(dto.getStart())
                .build();
    }

    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(UserMapper.toDto(booking.getBooker()))
                .status(booking.getStatus())
                .item(ItemMapper.toDto(booking.getItem()))
                .build();
    }
}
