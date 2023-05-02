package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserServiceImpl userServiceImpl;

    public ItemServiceImpl(@Qualifier("inMemoryItemStorage") ItemStorage itemStorage,
                           UserServiceImpl userServiceImpl) {
        this.itemStorage = itemStorage;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User user = UserMapper.toUser(userServiceImpl.getUser(userId));
        isBodyValid(itemDto);
        Item item = itemStorage.add(ItemMapper.toItem(itemDto, user));
        log.info("Вещь {} успешно добавлена.", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItem(long itemId) {
        return ItemMapper.toItemDto(itemStorage.findItem(itemId));
    }

    @Override
    public Collection<ItemDto> getItemsByUser(long userId) {
        userServiceImpl.getUser(userId);
        Collection<ItemDto> allItemsByUser = itemStorage.findAllItemsByUser(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info("Найденные вещи {} пользователя с ID {}", allItemsByUser, userId);
        return allItemsByUser;
    }

    @Override
    public Collection<ItemDto> getAll() {
        return itemStorage.findAll()
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(long userId, ItemDto itemDto, long itemId) {
        User user = UserMapper.toUser(userServiceImpl.getUser(userId));
        Item foundItem = itemStorage.findItem(itemId);

        if (!foundItem.getOwner().getId().equals(userId)) {
            log.warn("Не совпадают userId и ownerId.");
            throw new UserNotFoundException("Невозможно обновить вещь, т.к. она пренадлежит другому пользователю.");
        }

        if (itemDto.getName() == null) {
            itemDto.setName(foundItem.getName());
        }

        if (itemDto.getDescription() == null) {
            itemDto.setDescription(foundItem.getDescription());
        }

        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(foundItem.getAvailable());
        }

        itemDto.setId(itemId);
        itemStorage.update(ItemMapper.toItem(itemDto, user));

        log.info("Вещь {} успешно обновлена.", itemDto);
        return itemDto;
    }

    @Override
    public void delete(long itemId) {
        itemStorage.remove(itemId);
    }

    public Collection<ItemDto> searchItems(String text) {
        return itemStorage.searchItems(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void isBodyValid(ItemDto itemDto) {
        String name = itemDto.getName();
        String description = itemDto.getDescription();
        Boolean available = itemDto.getAvailable();

        if (name == null || name.isBlank()) {
            log.warn("Поле name пустое или содержит пробелы.");
            throw new ValidationException("Поле name не может быть пустым и содержать пробелы.");
        }

        if (description == null || description.isBlank()) {
            log.warn("Поле description пустое или содержит пробелы.");
            throw new ValidationException("Поле description не может быть пустым и содержать пробелы.");
        }

        if (available == null) {
            log.warn("Нет поля available.");
            throw new ValidationException("Поле available отсутствует.");
        }
        ResponseEntity.ok("valid");
    }
}
