package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@RestController
@RequestMapping(path = "/items")
@Slf4j
public class ItemController {
    private final ItemServiceImpl itemService;
    private final static String X_SHARER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemServiceImpl itemServiceImpl) {
        this.itemService = itemServiceImpl;
    }

    @PostMapping
    public Item createItem(@RequestHeader(X_SHARER) long userId, @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @GetMapping("/{id}")
    public Item getItemById(@PathVariable("id") long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<Item> getItemsByUser(@RequestHeader(X_SHARER) long userId) {
        return itemService.getItemsByUser(userId);
    }

    @PatchMapping("/{id}")
    public Item updateItem(@RequestHeader(X_SHARER) long userId,
                           @RequestBody Item item,
                           @PathVariable("id") long itemId
    ) {
        return itemService.update(userId, item, itemId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable("id") long itemId) {
        itemService.delete(itemId);
    }

    @GetMapping("/search")
    public Collection<Item> searchItems(@RequestParam(value = "text") String text) {
        return itemService.searchItems(text);
    }
}
