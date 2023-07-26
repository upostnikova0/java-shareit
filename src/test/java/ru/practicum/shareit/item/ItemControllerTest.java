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
class ItemControllerTest {
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
        userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
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
    void create_shouldReturnValidId() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(1L, itemDto);
        assertEquals(item.getId(), itemController.getById(item.getId(), user.getId()).getId());
    }

    @Test
    void create_shouldReturnValidItem() {
        UserDto user = userController.create(userDto);
        itemRequestController.create(user.getId(), itemRequestDto);
        itemDto.setRequestId(1L);
        userController.create(userDto.toBuilder().email("user2@email.com").build());
        ItemDto item = itemController.create(2L, itemDto);
        item.setComments(new ArrayList<>());
        assertEquals(item, itemController.getById(1L, 2L));
    }

    @Test
    void create_shouldReturnExceptionWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> itemController.create(1L, itemDto));
    }

    @Test
    void create_shouldReturnExceptionWhenInvalidItemId() {
        itemDto.setRequestId(10L);
        userController.create(userDto);
        assertThrows(ItemNotFoundException.class, () -> itemController.create(1L, itemDto));
    }

    @Test
    void update_shouldReturnValidDescription() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        ItemDto item = itemDto.toBuilder().name("new name").description("updateDescription").available(false).build();
        itemController.update(1L, item, 1L);
        assertEquals(item.getDescription(), itemController.getById(1L, 1L).getDescription());
    }

    @Test
    void update_shouldReturnExceptionWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> itemController.update(1L, itemDto, 1L));
    }

    @Test
    void update_shouldReturnExceptionWhenInvalidItemId() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertThrows(ItemNotFoundException.class,
                () -> itemController.update(1L, itemDto.toBuilder().name("new name").build(), 10L));
    }

    @Test
    void delete_shouldReturnValidListSize() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.getAllItemsByUser(1L).size());
        itemController.delete(1L);
        assertEquals(0, itemController.getAllItemsByUser(1L).size());
    }

    @Test
    void search_shouldReturnValidSize() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.search("Desc").size());
    }

    @Test
    void search_shouldReturnEmptyListWhenTextIsEmpty() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(new ArrayList<ItemDto>(), itemController.search(""));
    }

    @Test
    void getAllItemsByUser_shouldReturnValidListSize() {
        userController.create(userDto);
        itemController.create(1L, itemDto);

        ItemDto tempItemDto = ItemDto.builder()
                .name("tempItemDto")
                .description("tempItemDtoDescription")
                .available(true).build();

        itemController.create(1L, tempItemDto);

        assertEquals(2, itemController.getAllItemsByUser(1L).size());
    }

    @Test
    void search_shouldReturnEmptyList() {
        assertEquals(new ArrayList<ItemDto>(), itemController.search("t"));
    }

    @Test
    void addComment_shouldReturnValidCommentListSize() {
        userController.create(userDto);
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
    void addComment_shouldReturnExceptionWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> itemController.addComment(1L, comment, 1L));
    }

    @Test
    void addComment_shouldReturnException() {
        userController.create(userDto);
        assertThrows(ItemNotFoundException.class, () -> itemController.addComment(1L, comment, 1L));
        itemController.create(1L, itemDto);
        assertThrows(BadRequestException.class, () -> itemController.addComment(1L, comment, 1L));
    }

    @Test
    void getAllItemsByUser_shouldReturnExceptionWhenWhenInvalidUserId() {
        assertThrows(UserNotFoundException.class, () -> itemController.getAllItemsByUser(1L));
    }
}
