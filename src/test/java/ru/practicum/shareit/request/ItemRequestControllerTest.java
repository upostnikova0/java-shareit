package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTest {
    @Autowired
    private ItemRequestController itemRequestController;
    @Autowired
    private UserController userController;
    @Autowired
    private ItemController itemController;
    private ItemRequestDto itemRequestDto;
    private UserDto userDto;
    private ItemDto itemDto;

    @BeforeEach
    void init() {
        userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .description("item request description")
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

    @Test
    void getAllRequests_shouldReturnExceptionWhenWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemRequestController.getAllRequests(1L, 10, 1));
    }

    @Test
    void getAllRequests_shouldReturnExceptionWhenWrongFrom() {
        assertThrows(BadRequestException.class, () -> itemRequestController.getAllRequests(1L, -10, 1));
    }

    @Test
    void getAllRequests_shouldReturnValidListSize() {
        UserDto requester = userController.create(userDto);
        ItemRequestDto itemRequestDto1 = itemRequestController.create(requester.getId(), itemRequestDto);

        UserDto tempUser = UserDto.builder()
                .name("tempUser")
                .email("temp@mail.ru").build();

        itemDto = ItemDto.builder()
                .name("itemName")
                .description("itemDesc")
                .available(true)
                .requestId(itemRequestDto1.getId()).build();

        UserDto owner = userController.create(tempUser);
        itemController.create(owner.getId(), itemDto);

        assertEquals(1, itemRequestController.getAllRequests(owner.getId(), 1, 10).size());
    }
}