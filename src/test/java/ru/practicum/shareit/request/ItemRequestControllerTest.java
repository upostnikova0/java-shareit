package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTest {
    @Autowired
    private ItemRequestController itemRequestController;

    @Autowired
    private UserController userController;

    private ItemRequestDto itemRequestDto;

    private UserDto userDto;

    @BeforeEach
    void init() {
        itemRequestDto = ItemRequestDto.builder()
                .description("item request description")
                .build();

        userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();
    }

    @Test
    void create_shouldReturnValidUserId() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(user.getId(), itemRequestDto);
        assertEquals(1L, itemRequestController.getById(itemRequest.getId(), user.getId()).getId());
    }

    @Test
    void create_shouldReturnExceptionWhenWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemRequestController.create(1L, itemRequestDto));
    }

    @Test
    void getAllRequestsByOwner_shouldReturnValidListSize() {
        UserDto user = userController.create(userDto);
        itemRequestController.create(user.getId(), itemRequestDto);
        assertEquals(1, itemRequestController.getAllRequestsByOwner(user.getId()).size());
    }

    @Test
    void getAllRequestsByOwner_shouldReturnExceptionWhenWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemRequestController.getAllRequestsByOwner(1L));
    }

//    @Test
//    void getAll() {
//        UserDto user = userController.create(userDto);
//        itemRequestController.create(user.getId(), itemRequestDto);
//        assertEquals(0, itemRequestController.getAllRequests(user.getId(), 10, 1).size());
//        UserDto user2 = userController.create(userDto.toBuilder().email("user1@email.com").build());
//        assertEquals(1, itemRequestController.getAllRequests(user2.getId(), 10, 1).size());
//    }

    @Test
    void getAllRequests_shouldReturnExceptionWhenWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemRequestController.getAllRequests(1L, 10, 1));
    }

    @Test
    void getAllRequests_shouldReturnExceptionWhenWrongFrom() {
        assertThrows(BadRequestException.class, () -> itemRequestController.getAllRequests(1L, -10, 1));
    }
}