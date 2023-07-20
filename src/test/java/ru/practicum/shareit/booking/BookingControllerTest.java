package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {
    @Autowired
    private BookingController bookingController;
    @Autowired
    private UserController userController;
    @Autowired
    private ItemController itemController;
    private ItemDto itemDto;
    private UserDto userDto;
    private UserDto userDto1;
    private BookingDto bookingDto;

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

        userDto1 = UserDto.builder()
                .name("name")
                .email("user1@email.com")
                .build();

        bookingDto = BookingDto.builder()
                .start(LocalDateTime.of(2022, 10, 24, 12, 30))
                .end(LocalDateTime.of(2023, 11, 10, 13, 0))
                .itemId(1L).build();
    }

    @Test
    void create_shouldReturnValidBookingId() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(user1.getId(), bookingDto);
        assertEquals(1L, bookingController.getById(user1.getId(), booking.getId()).getId());
    }

    @Test
    void create_shouldReturnExceptionWhenWrongUser() {
        assertThrows(UserNotFoundException.class, () -> bookingController.create(1L, bookingDto));
    }

    @Test
    void create_shouldReturnExceptionWhenWrongItem() {
        userController.create(userDto);
        assertThrows(ItemNotFoundException.class, () -> bookingController.create(1L, bookingDto));
    }

    @Test
    void create_shouldReturnExceptionWhenWrongBooking() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        assertThrows(BookingNotFoundException.class, () -> bookingController.create(1L, bookingDto));
    }

    @Test
    void create_shouldReturnExceptionWhenUnavailableItem() {
        UserDto user = userController.create(userDto);
        itemDto.setAvailable(false);
        itemController.create(user.getId(), itemDto);
        userController.create(userDto1);
        assertThrows(ValidationException.class, () -> bookingController.create(2L, bookingDto));
    }

    @Test
    void create_shouldReturnExceptionWhenWrongEndDate() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        bookingDto.setEnd(LocalDateTime.of(2022, 9, 24, 12, 30));
        assertThrows(ValidationException.class, () -> bookingController.create(user1.getId(), bookingDto));
    }

//    @Test
//    void approveTest() {
//        UserDto user = userController.create(userDto);
//        ItemDto item = itemController.create(user.getId(), itemDto);
//        UserDto user1 = userController.create(userDto1);
//        BookingDto booking = bookingController.create(user1.getId(), BookingDto.builder()
//                .start(LocalDateTime.of(2022, 10, 24, 12, 30))
//                .end(LocalDateTime.of(2022, 11, 10, 13, 0))
//                .itemId(item.getId()).build());
//        assertEquals(Status.WAITING, bookingController.getById(booking.getId(), user1.getId()).getStatus());
//        bookingController.updateBookingStatus(booking.getId(), user.getId(), true);
//        assertEquals(Status.APPROVED, bookingController.getById(booking.getId(), user1.getId()).getStatus());
//    }

    @Test
    void updateBookingStatus_shouldReturnExceptionWhenWrongUserId() {
        assertThrows(UserNotFoundException.class, () -> bookingController.updateBookingStatus(1L, 1L, true));
    }

    @Test
    void updateBookingStatus_shouldReturnExceptionWhenWrongBookingId() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        bookingController.create(user1.getId(), bookingDto);
        assertThrows(BookingNotFoundException.class, () -> bookingController.updateBookingStatus(1L, 2L, true));
    }

    @Test
    void updateBookingStatus_shouldReturnExceptionWhenWrongStatus() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        bookingController.create(user1.getId(), bookingDto);
        bookingController.updateBookingStatus(1L, 1L, true);
        assertThrows(ValidationException.class, () -> bookingController.updateBookingStatus(1L, 1L, true));
    }

    @Test
    void getAll_shouldReturnValidListSize() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(user1.getId(), bookingDto);
        assertEquals(1, bookingController.getAllBookingsByUser(user1.getId(), State.WAITING, 0, 10).size());
        assertEquals(1, bookingController.getAllBookingsByUser(user1.getId(), State.ALL, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByUser(user1.getId(), State.PAST, 0, 10).size());
        assertEquals(1, bookingController.getAllBookingsByUser(user1.getId(), State.CURRENT, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByUser(user1.getId(), State.FUTURE, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByUser(user1.getId(), State.REJECTED, 0, 10).size());
        bookingController.updateBookingStatus(booking.getId(), user.getId(), true);
        assertEquals(1, bookingController.getAllBookingsByOwner(user.getId(), State.CURRENT, 0, 10).size());
        assertEquals(1, bookingController.getAllBookingsByOwner(user.getId(), State.ALL, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByOwner(user.getId(), State.WAITING, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByOwner(user.getId(), State.WAITING, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByOwner(user.getId(), State.REJECTED, 0, 10).size());
        assertEquals(0, bookingController.getAllBookingsByOwner(user.getId(), State.PAST, 0, 10).size());
    }

    @Test
    void getAll_shouldReturnExceptionWhenWrongUserId() {
        assertThrows(UserNotFoundException.class, () -> bookingController.getAllBookingsByUser(1L, State.ALL, 0, 10));
        assertThrows(UserNotFoundException.class, () -> bookingController.getAllBookingsByOwner(1L, State.ALL, 0, 10));
    }

    @Test
    void getById_shouldReturnExceptionWhenWrongUserId() {
        assertThrows(UserNotFoundException.class, () -> bookingController.getById(1L, 1L));
    }

    @Test
    void getById_ShouldReturnExceptionWhenWrongBookingId() {
        UserDto user = userController.create(userDto);
        itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        bookingController.create(user1.getId(), bookingDto);
        assertThrows(BookingNotFoundException.class, () -> bookingController.getById(1L, 10L));
    }
}
