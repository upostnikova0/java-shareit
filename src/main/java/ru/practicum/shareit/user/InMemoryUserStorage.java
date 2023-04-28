package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private static Long userId = 0L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        long id = getUserId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User find(long userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            log.warn("Пользователь с ID {} не найден.", userId);
            throw new UserNotFoundException(String.format("Пользователь с ID %d не найден.", userId));
        }
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User update(User user) {
        return users.put(user.getId(), user);
    }

    @Override
    public void remove(User user) {
        users.remove(user.getId());
    }

    private Long getUserId() {
        return ++userId;
    }

    @Override
    public boolean isEmailExists(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
}
