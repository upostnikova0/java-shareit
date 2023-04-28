package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.exceptions.UserValidationException;

import java.util.Collection;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemServiceImpl(@Qualifier("inMemoryItemStorage") ItemStorage itemStorage,
                           UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    public Item create(long userId, ItemDto itemDto) {
        User user = userService.getUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        isBodyValid(item);
        item.setOwner(user);
        Item newItem = itemStorage.add(item);
        log.info("Вещь {} успешно добавлена.", newItem);
        return newItem;
    }

    public Item getItem(long itemId) {
        return itemStorage.findItem(itemId);
    }

    public Collection<Item> getItemsByUser(long userId) {
        userService.getUser(userId);
        Collection<Item> allItemsByUser = itemStorage.findAllItemsByUser(userId);
        log.info("Найденные вещи {} пользователя с ID {}", allItemsByUser, userId);
        return allItemsByUser;
    }

    public Item update(long userId, Item item, long itemId) {
        User user = userService.getUser(userId);
        Item foundItem = itemStorage.findItem(itemId);

        if (!foundItem.getOwner().getId().equals(userId)) {
            log.warn("Не совпадают userId и ownerId.");
            throw new UserNotFoundException("Невозможно обновить вещь, т.к. она пренадлежит другому пользователю.");
        }

        if (item.getName() == null) {
            item.setName(foundItem.getName());
        }

        if (item.getDescription() == null) {
            item.setDescription(foundItem.getDescription());
        }

        if (item.getAvailable() == null) {
            item.setAvailable(foundItem.getAvailable());
        }

        item.setId(itemId);
        item.setOwner(user);
        itemStorage.update(item);

        log.info("Вещь {} успешно обновлена.", item);
        return item;
    }

    public void delete(long itemId) {
        itemStorage.remove(itemId);
    }

    public Collection<Item> searchItems(String text) {
        return itemStorage.searchItems(text);
    }

    private void isBodyValid(Item item) {
        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();

        if (name == null || name.isBlank()) {
            log.warn("Поле name пустое или содержит пробелы.");
            throw new UserValidationException("Поле name не может быть пустым и содержать пробелы.");
        }

        if (description == null || description.isBlank()) {
            log.warn("Поле description пустое или содержит пробелы.");
            throw new UserValidationException("Поле description не может быть пустым и содержать пробелы.");
        }

        if (available == null) {
            log.warn("Нет поля available.");
            throw new UserValidationException("Поле available отсутствует.");
        }
        ResponseEntity.ok("valid");
    }
}
