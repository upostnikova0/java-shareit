package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exceptions.EmailAlreadyExistException;
import ru.practicum.shareit.user.exceptions.UserValidationException;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;


    public UserService(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        isBodyValid(user);
        if (!userStorage.isEmailExists(user.getEmail())) {
            User newUser = userStorage.add(user);
            log.info("Пользователь {} успешно добавлен", user);
            return newUser;
        } else {
            log.warn("Не удалось добавить пользователя.");
            throw new EmailAlreadyExistException("Пользователь с таким email уже существует.");
        }
    }

    public User getUser(long userId) {
        return userStorage.find(userId);
    }

    public Collection<User> getAll() {
        return userStorage.findAll();
    }

    public User update(User user, long userId) throws UserValidationException {
        User foundUser = userStorage.find(userId);
        String email = user.getEmail();

        if (email != null) {
            if (!email.equals(foundUser.getEmail()) && userStorage.isEmailExists(email)) {
                log.warn("Невозможно добавить пользователя из-за существующего email: {}", email);
                throw new EmailAlreadyExistException("Пользователь с таким email уже существует.");
            }
        } else {
            user.setEmail(foundUser.getEmail());
        }

        user.setId(userId);
        if (user.getName() == null) {
            user.setName(foundUser.getName());
        }

        userStorage.update(user);
        log.info("Пользователь {} успешно обновлен", user);
        return user;
    }

    public void delete(long userId) {
        User user = userStorage.find(userId);
        userStorage.remove(user);
    }

    private void isBodyValid(User user) {
        String email = user.getEmail();
        String name = user.getName();
        if (email == null || email.isEmpty() || email.isBlank() || !email.contains("@")) {
            log.warn("Поле email пустое или не содержит символ @.");
            throw new UserValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }

        if (name == null || name.isBlank()) {
            log.warn("Поле name пустое или содержит пробелы.");
            throw new UserValidationException("Имя не может быть пустым и содержать пробелы.");
        }

        ResponseEntity.ok("valid");
    }
}
