package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTests {
    @Autowired
    private ItemController itemController;

    @Autowired
    private UserController userController;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private ItemRequestController itemRequestController;

    private ItemDto itemDto;

    private UserDto userDto;

    private ItemRequestDto itemRequestDto;

    private CommentDto comment;

    @BeforeEach
    void init() {
        itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        itemRequestDto = ItemRequestDto
                .builder()
                .description("item request description")
                .build();

        comment = CommentDto
                .builder()
                .text("first comment")
                .build();
    }

    @Test
    void createTest() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(1L, itemDto);
        assertEquals(item.getId(), itemController.getById(item.getId(), user.getId()).getId());
    }

    @Test
    void createWithRequestTest() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(user.getId(), itemRequestDto);
        itemDto.setRequestId(1L);
        UserDto user2 = userController.create(userDto.toBuilder().email("user2@email.com").build());
        ItemDto item = itemController.create(2L, itemDto);
        item.setComments(new ArrayList<>());
        assertEquals(item, itemController.getById(1L, 2L));
    }

    @Test
    void createByWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemController.create(1L, itemDto));
    }

    @Test
    void createWithWrongItemRequest() {
        itemDto.setRequestId(10L);
        UserDto user = userController.create(userDto);
        assertThrows(ItemNotFoundException.class, () -> itemController.create(1L, itemDto));
    }

    @Test
    void updateTest() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        ItemDto item = itemDto.toBuilder().name("new name").description("updateDescription").available(false).build();
        itemController.update(1L, item, 1L);
        assertEquals(item.getDescription(), itemController.getById(1L, 1L).getDescription());
    }

    @Test
    void updateForWrongItemTest() {
        assertThrows(UserNotFoundException.class, () -> itemController.update(1L, itemDto, 1L));
    }

    @Test
    void updateByWrongUserTest() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertThrows(ItemNotFoundException.class,
                () -> itemController.update(1L, itemDto.toBuilder().name("new name").build(), 10L));
    }

    @Test
    void deleteTest() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.getAllItemsByUser(1L).size());
        itemController.delete(1L);
        assertEquals(0, itemController.getAllItemsByUser(1L).size());
    }

    @Test
    void searchTest() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.search("Desc").size());
    }

    @Test
    void searchEmptyTextTest() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(new ArrayList<ItemDto>(), itemController.search(""));
    }

    @Test
    void search_shouldReturnEmptyList() {
        assertEquals(new ArrayList<ItemDto>(), itemController.search("t"));
    }

    @Test
    void createCommentTest() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(1L, itemDto);
        UserDto user2 = userController.create(userDto.toBuilder().email("email2@mail.com").build());
        bookingController.create(user2.getId(), BookingDto.builder()
                .start(LocalDateTime.of(2022, 10, 20, 12, 15))
                .end(LocalDateTime.of(2022, 10, 27, 12, 15))
                .itemId(item.getId()).build());
        bookingController.updateBookingStatus(1L, 1L, true);
        itemController.addComment(user2.getId(), comment, item.getId());
        assertEquals(1, itemController.getById(1L, 1L).getComments().size());
    }

    @Test
    void createCommentByWrongUser() {
        assertThrows(UserNotFoundException.class, () -> itemController.addComment(1L, comment, 1L));
    }

    @Test
    void createCommentToWrongItem() {
        UserDto user = userController.create(userDto);
        assertThrows(ItemNotFoundException.class, () -> itemController.addComment(1L, comment, 1L));
        ItemDto item = itemController.create(1L, itemDto);
        assertThrows(BadRequestException.class, () -> itemController.addComment(1L, comment, 1L));
    }

    @Test
    void getAllWithWrongFrom() {
        assertThrows(UserNotFoundException.class, () -> itemController.getAllItemsByUser(1L));
    }
}
