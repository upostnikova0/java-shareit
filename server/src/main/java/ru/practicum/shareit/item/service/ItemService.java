package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto getById(Long itemId, Long ownerId);

    List<ItemDto> getAllItemsByUser(Long userId);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    void delete(Long itemId);

    Collection<ItemDto> searchItems(String text);

    CommentDto addComment(long userId, CommentDto commentDtoFromUser, long itemId);
}
