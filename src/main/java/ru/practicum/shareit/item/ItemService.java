package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    Item create(long userId, ItemDto itemDto);

    Item getItem(long itemId);

    Collection<Item> getItemsByUser(long userId);

    Item update(long userId, Item item, long itemId);

    void delete(long itemId);

    Collection<Item> searchItems(String text);
}
