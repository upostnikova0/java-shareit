package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService, Create, Update {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(@Qualifier("userRepository") UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User newUser = userRepository.save(userMapper.toUser(userDto));
        log.info("Добавлен пользователь {}.", newUser);
        return userMapper.toUserDto(newUser);
    }

    @Override
    public UserDto getUser(Long userId) {
        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isPresent()) {
            log.info("Найден пользователь {}", foundUser.get());
            return userMapper.toUserDto(foundUser.get());
        } else {
            log.warn(String.format("Не найден пользователь с ID %d.", userId));
            throw new UserNotFoundException(String.format("Не найден пользователь с ID %d.", userId));
        }
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User foundUser = userMapper.toUser(getUser(userId));
        String email = userDto.getEmail();

        if (email != null) {
            if (!email.equals(foundUser.getEmail()) && userRepository.findByEmailEquals(email).isPresent()) {
                log.warn("Невозможно обновить пользователя из-за существующего email: {}", email);
                throw new EmailAlreadyExistException("Пользователь с таким email уже существует.");
            }
        } else {
            userDto.setEmail(foundUser.getEmail());
        }

        userDto.setId(userId);
        if (userDto.getName() == null) {
            userDto.setName(foundUser.getName());
        }

        userRepository.save(userMapper.toUser(userDto));
        log.info("Обновлен пользователь {}", userDto);
        return userDto;
    }

    @Override
    public void delete(Long userId) {
        User user = userMapper.toUser(getUser(userId));

        log.info("Удален пользователь {}", user);
        userRepository.deleteById(user.getId());
    }
}
