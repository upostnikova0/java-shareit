package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserStorage {
    User add(User user);

    User find(long userId);

    Collection<User> findAll();

    User update(User user);

    void remove(User user);

    boolean isEmailExists(String email);
}
