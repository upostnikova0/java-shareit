package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.comment.CommentDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto getItem(Long id, Long ownerId);

    Collection<ItemDto> getAllItemsByUser(Long userId);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    void delete(Long itemId);

    Collection<ItemDto> searchItems(String text);

    CommentDto addComment(long userId, CommentDto commentDtoFromUser, long itemId);
}
