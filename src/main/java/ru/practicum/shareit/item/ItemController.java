package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ItemDto> createItem(@RequestHeader(xSharerUserId) long userId, @RequestBody ItemDto itemDto) {
        return new ResponseEntity<>(itemService.create(userId, itemDto), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable("id") long itemId) {
        return new ResponseEntity<>(itemService.getItem(itemId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getItemsByUser(@RequestHeader(xSharerUserId) long userId) {
        return new ResponseEntity<>(itemService.getItemsByUser(userId), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(xSharerUserId) long userId,
                                              @RequestBody ItemDto itemDto,
                                              @PathVariable("id") long itemId
    ) {
        return new ResponseEntity<>(itemService.update(userId, itemDto, itemId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable("id") long itemId) {
        itemService.delete(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> searchItems(@RequestParam(value = "text") String text) {
        return new ResponseEntity<>(itemService.searchItems(text), HttpStatus.OK);
    }
}
