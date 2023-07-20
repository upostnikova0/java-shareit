package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.item.ItemController.xSharerUserId;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    BookingService bookingService;

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
    public List<BookingDto> getAllBookingsByUser(@RequestHeader(xSharerUserId) Long userId,
                                                 @RequestParam(defaultValue = "ALL", required = false) State state,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Индекс первого элемента и кол-во элементов должны быть больше нуля.");
        }
        return bookingService.getAllBookingsByUser(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(@RequestHeader(xSharerUserId) Long userId,
                                                  @RequestParam(defaultValue = "ALL", required = false) State state,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Индекс первого элемента и кол-во элементов должны быть больше нуля.");
        }
        return bookingService.getAllBookingsByOwner(userId, state, from, size);
    }
}
