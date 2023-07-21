package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired
    private UserController userController;
    private UserDto user;
    private UserDto tempUser;

    @BeforeEach
    void init() {
        user = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        tempUser = UserDto.builder()
                .name("Temp")
                .email("temp@mail.ru").build();
    }

    @Test
    void create() {
        UserDto userDto = userController.create(user);
        assertEquals(userDto.getId(), userController.getById(userDto.getId()).getId());
    }

    @Test
    void update() {
        userController.create(user);
        UserDto userDto = user.toBuilder().name("update name").email("update@email.com").build();
        userController.update(userDto, 1L);
        assertEquals(userDto.getEmail(), userController.getById(1L).getEmail());
    }

    @Test
    void update_shouldReturnExceptionWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> userController.update(user, 1L));
    }

    @Test
    void update_shouldTReturnExceptionWhenEmailAlreadyExist() {
        userController.create(user);
        userController.create(tempUser);
        tempUser.setEmail(user.getEmail());
        assertThrows(EmailAlreadyExistException.class, () -> userController.update(tempUser, 2L));
    }

    @Test
    void delete() {
        UserDto userDto = userController.create(user);
        assertEquals(1, userController.getAll().size());
        userController.delete(userDto.getId());
        assertEquals(0, userController.getAll().size());
    }

    @Test
    void getById_shouldReturnExceptionWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> userController.getById(1L));
    }
}