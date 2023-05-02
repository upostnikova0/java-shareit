package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.UserDto;

public interface Update {
    UserDto update(UserDto userDto, long userId);
}
