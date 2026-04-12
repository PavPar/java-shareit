package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Qualifier("itemServiceImpl")
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;


    @Override
    @Transactional
    public Collection<ItemDto> getAll(Long ownerId) {
        List<Item> items = new ArrayList<>();

        if (Objects.isNull(ownerId)) {
            items = repository.findAll();
            return items.stream()
                    .map(this::getItemDtoWithComments)
                    .collect(Collectors.toList());

        }

        items = repository.findByOwnerId(ownerId);
        return items.stream()
                .map(this::getItemDtoWithCommentsAndBookings)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto getOne(Long ownerId, long id) {
        Item item = repository.findById(id).orElseThrow(() -> new NotFoundException("no item"));
        if (Objects.equals(item.getOwnerId(), ownerId)) {
            return this.getItemDtoWithCommentsAndBookings(item);
        }
        return this.getItemDtoWithComments(item);
    }

    @Override
    @Transactional
    public ItemDto add(Long ownerId, ItemCreateDto dto) {
        userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("no user"));
        Item item = ItemMapper.toEntity(dto);
        item.setOwnerId(ownerId);
        Item addedItem = repository.save(item);

        return ItemMapper.toDto(addedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, long id, ItemUpdateDto dto) {
        Item existingItem = repository.findById(id).orElseThrow(() -> new NotFoundException("no item"));
        if (!Objects.equals(existingItem.getOwnerId(), ownerId)) {
            throw new ForbiddenException("not an owner");
        }
        if (!Objects.isNull(dto.getName())) {
            existingItem.setName(dto.getName());
        }
        if (!Objects.isNull(dto.getDescription())) {
            existingItem.setDescription(dto.getDescription());
        }
        if (!Objects.isNull(dto.getAvailable())) {
            existingItem.setAvailable(dto.getAvailable());
        }

        return ItemMapper.toDto(existingItem);
    }

    @Override
    public Collection<ItemDto> search(String query) {
        if (query.isBlank()) {
            return new ArrayList<>();
        }
        return repository.search(query).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("no user"));
        Item item = repository.findById(itemId).orElseThrow(() -> new NotFoundException("no item"));
        List<Booking> booking = bookingRepository.findAllByBookerId(author.getId());

        if (!bookingRepository.existsCompletedBookingByItemAndUser(itemId, userId)) {
            throw new ValidationException("didn't book this item");
        }

        Comment comment = CommentMapper.toEntity(dto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private ItemDto getItemDtoWithComments(Item item) {
        return ItemMapper.toDto(item, commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()));
    }

    private ItemDto getItemDtoWithCommentsAndBookings(Item item) {
        LocalDateTime currentTime = LocalDateTime.now();
        return ItemMapper.toDto(item,
                commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()),
                bookingRepository.findLastBooking(item.getId(), currentTime).orElse(null),
                bookingRepository.findNextBooking(item.getId(), currentTime).orElse(null)
        );
    }


}
