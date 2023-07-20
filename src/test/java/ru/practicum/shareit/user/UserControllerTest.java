package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired
    private UserController userController;

    private UserDto user;

    @BeforeEach
    void init() {
        user = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();
    }

    @Test
    void createTest() {
        UserDto userDto = userController.create(user);
        assertEquals(userDto.getId(), userController.getById(userDto.getId()).getId());
    }

    @Test
    void updateTest() {
        userController.create(user);
        UserDto userDto = user.toBuilder().name("update name").email("update@email.com").build();
        userController.update(userDto, 1L);
        assertEquals(userDto.getEmail(), userController.getById(1L).getEmail());
    }

    @Test
    void updateByWrongUserTest() {
        assertThrows(UserNotFoundException.class, () -> userController.update(user, 1L));
    }

    @Test
    void deleteTest() {
        UserDto userDto = userController.create(user);
        assertEquals(1, userController.getAll().size());
        userController.delete(userDto.getId());
        assertEquals(0, userController.getAll().size());
    }

    @Test
    void getByWrongIdTest() {
        assertThrows(UserNotFoundException.class, () -> userController.getById(1L));
    }
}