package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    ItemShortDto create(Long userId, ItemShortDto itemShortDto);

    ItemDto getById(Long itemId, Long ownerId);

    List<ItemDto> getAllItemsByUser(Long userId, Integer from, Integer size);

    ItemShortDto update(Long userId, ItemShortDto itemShortDto, Long itemId);

    void delete(Long itemId);

    Collection<ItemShortDto> searchItems(String text, Integer from, Integer size);

    CommentDto addComment(long userId, CommentDto commentDtoFromUser, long itemId);
}
