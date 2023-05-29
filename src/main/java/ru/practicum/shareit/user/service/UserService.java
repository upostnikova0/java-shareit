package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto getUser(Long userId);

    Collection<UserDto> getAll();

    UserDto update(UserDto userDto, Long userId);

    void delete(Long userId);
}
