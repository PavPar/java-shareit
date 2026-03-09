package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public List<ItemRequestDto> getAll() {
        return populateItemRequestWithItem(repository.findAllByOrderByCreatedDesc());
    }

    @Transactional
    public List<ItemRequestDto> getAllFromUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("no user"));
        return populateItemRequestWithItem(repository.findAllByRequesterId(userId));
    }

    @Transactional
    public ItemRequestDto getById(Long id) {
        return populateItemRequestWithItem(repository.findById(id).orElseThrow(NotFoundException::new));
    }

    @Transactional
    public ItemRequestDto save(Long userId, ItemRequestCreateDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("no user"));
        ItemRequest itemRequest = ItemRequestMapper.toEntity(dto, user);
        return ItemRequestMapper.toDto(repository.save(itemRequest));
    }

    private List<ItemRequestDto> populateItemRequestWithItem(List<ItemRequest> requests) {
        List<ItemRequestDto> requestsDto = new ArrayList<>();

        for (ItemRequest itemRequest : requests) {
            requestsDto.add(populateItemRequestWithItem(itemRequest));
        }

        return requestsDto;
    }

    private ItemRequestDto populateItemRequestWithItem(ItemRequest request) {
        List<Item> items = itemRepository.findByRequestId(request.getId());
        return ItemRequestMapper.toDto(request, items);
    }
}
