package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllRequestsByOwner(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getById(Long requestId, Long userId);
}