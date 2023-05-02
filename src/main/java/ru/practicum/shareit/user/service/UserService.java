package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto getUser(long userId);

    Collection<UserDto> getAll();

    void delete(long userId);
}
