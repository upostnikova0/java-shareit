package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.Valid;
import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    BookingService bookingService;
    public static final String xSharerUserId = "X-Sharer-User-Id";

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestHeader(xSharerUserId) Long userId,
                             @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{id}")
    public BookingDto updateBookingStatus(@RequestHeader(xSharerUserId) Long userId,
                                          @PathVariable Long id,
                                          @RequestParam Boolean approved) {
        return bookingService.updateBookingStatus(userId, id, approved);
    }

    @GetMapping("/{id}")
    public BookingDto getById(@RequestHeader(xSharerUserId) Long userId,
                              @PathVariable Long id) {
        return bookingService.getById(userId, id);
    }

    @GetMapping
    public Collection<BookingDto> getAllBookingsByUser(@RequestHeader(xSharerUserId) Long userId,
                                                       @RequestParam(defaultValue = "ALL", required = false) State state) {
        return bookingService.getAllBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getAllBookingsByOwner(@RequestHeader(xSharerUserId) Long userId,
                                                        @RequestParam(defaultValue = "ALL", required = false) State state) {
        return bookingService.getAllBookingsByOwner(userId, state);
    }
}
