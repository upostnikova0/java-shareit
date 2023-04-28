package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemStorage {
    Item add(Item item);

    Item findItem(long itemId);

    Collection<Item> findAllItemsByUser(long userId);

    Item update(Item item);

    void remove(long itemId);

    Collection<Item> searchItems(String text);

    Map<Long, Item> getItems();
}
