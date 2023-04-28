package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryItemStorage")
public class InMemoryItemStorage implements ItemStorage {
    private static Long itemId = 0L;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        long itemId = getItemId();
        item.setId(itemId);
        items.put(itemId, item);
        return item;
    }

    @Override
    public Item findItem(long itemId) {
        if (items.containsKey(itemId)) {
            return items.get(itemId);
        } else {
            log.warn("Вещь с ID {} не найдена.", itemId);
            throw new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", itemId));
        }
    }

    @Override
    public Collection<Item> findAllItemsByUser(long userId) {
        return items.values()
                .stream()
                .filter(x -> x.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item update(Item item) {
        return items.put(item.getId(), item);
    }

    @Override
    public void remove(long itemId) {
        items.remove(itemId);
    }

    @Override
    public Collection<Item> searchItems(String text) {
        List<Item> foundItems = new ArrayList<>();

        if (text.isBlank()) {
            return foundItems;
        }

        text = text.toLowerCase();
        String name;
        String description;
        boolean available;

        for (Item item : items.values()) {
            name = item.getName().toLowerCase();
            description = item.getDescription().toLowerCase();
            available = item.getAvailable();

            if (name.contains(text) || description.contains(text)) {
                if (available) {
                    foundItems.add(item);
                }
            }
        }
        return foundItems;
    }

    @Override
    public Map<Long, Item> getItems() {
        return null;
    }

    private Long getItemId() {
        return ++itemId;
    }
}

