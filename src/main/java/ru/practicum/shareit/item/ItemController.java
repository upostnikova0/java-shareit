package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private final String xSharerUserId = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(xSharerUserId) long userId, @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable("id") long itemId) {
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getItemsByUser(@RequestHeader(xSharerUserId) long userId) {
        return itemService.getItemsByUser(userId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader(xSharerUserId) long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable("id") long itemId
    ) {
        return itemService.update(userId, itemDto, itemId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable("id") long itemId) {
        itemService.delete(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam(value = "text") String text) {
        return itemService.searchItems(text);
    }
}
