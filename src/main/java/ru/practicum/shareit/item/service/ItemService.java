package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto getItem(long itemId);

    Collection<ItemDto> getItemsByUser(long userId);

    Collection<ItemDto> getAll();

    ItemDto update(long userId, ItemDto itemDto, long itemId);

    void delete(long itemId);

    Collection<ItemDto> searchItems(String text);
}
