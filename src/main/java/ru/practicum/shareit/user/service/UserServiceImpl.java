package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exceptions.EmailAlreadyExistException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.model.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService, Create, Update {
    private final UserStorage userStorage;

    public UserServiceImpl(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        isBodyValid(userDto);
        if (!userStorage.isEmailExists(userDto.getEmail())) {
            User newUser = userStorage.add(UserMapper.toUser(userDto));
            log.info("Пользователь {} успешно добавлен", newUser);
            return UserMapper.toUserDto(newUser);
        } else {
            log.warn("Не удалось добавить пользователя.");
            throw new EmailAlreadyExistException("Пользователь с таким email уже существует.");
        }
    }

    @Override
    public UserDto getUser(long userId) {
        return UserMapper.toUserDto(userStorage.find(userId));
    }

    @Override
    public Collection<UserDto> getAll() {
        return userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto userDto, long userId) {
        User foundUser = userStorage.find(userId);
        String email = userDto.getEmail();

        if (email != null) {
            if (!email.equals(foundUser.getEmail()) && userStorage.isEmailExists(email)) {
                log.warn("Невозможно добавить пользователя из-за существующего email: {}", email);
                throw new EmailAlreadyExistException("Пользователь с таким email уже существует.");
            }
        } else {
            userDto.setEmail(foundUser.getEmail());
        }

        userDto.setId(userId);
        if (userDto.getName() == null) {
            userDto.setName(foundUser.getName());
        }

        userStorage.update(UserMapper.toUser(userDto));
        log.info("Пользователь {} успешно обновлен", userDto);
        return userDto;
    }

    @Override
    public void delete(long userId) {
        User user = userStorage.find(userId);
        log.info("Пользователь {} успешно удален.", user);
        userStorage.remove(user);
    }

    private void isBodyValid(UserDto userDto) {
        String email = userDto.getEmail();
        String name = userDto.getName();

        if (email == null || email.isEmpty() || email.isBlank() || !email.contains("@")) {
            log.warn("Поле email пустое или не содержит символ @.");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }

        if (name == null || name.isBlank()) {
            log.warn("Поле name пустое или содержит пробелы.");
            throw new ValidationException("Имя не может быть пустым и содержать пробелы.");
        }

        ResponseEntity.ok("valid");
    }
}
