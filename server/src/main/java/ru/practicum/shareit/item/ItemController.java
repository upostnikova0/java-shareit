package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;
    public static final String xSharerUserId = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(xSharerUserId) Long userId, @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable Long id, @RequestHeader(xSharerUserId) Long userId) {
        return itemService.getById(id, userId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByUser(@RequestHeader(xSharerUserId) Long userId) {
        return itemService.getAllItemsByUser(userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(xSharerUserId) Long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable("id") Long itemId
    ) {
        return itemService.update(userId, itemDto, itemId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long itemId) {
        itemService.delete(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(value = "text") String text) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@RequestHeader(xSharerUserId) Long userId,
                                 @RequestBody CommentDto commentDto,
                                 @PathVariable Long id) {
        return itemService.addComment(userId, commentDto, id);
    }
}
