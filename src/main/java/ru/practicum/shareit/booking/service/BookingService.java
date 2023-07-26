package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    BookingDto create(Long userId, BookingDto bookingDtoFromUser);

    BookingDto getById(Long userId, Long id);

    List<BookingDto> getAllBookingsByUser(Long userId, State value, Integer from, Integer size);

    List<BookingDto> getAllBookingsByOwner(Long userId, State value, Integer from, Integer size);

    BookingDto updateBookingStatus(Long userId, Long id, Boolean value);

    Optional<Booking> findLastItem(Long itemId);

    Optional<Booking> findNextItem(Long itemId);

    List<Booking> findAllByBookerIdAndItemId(Long userId, Long itemId, Status status, LocalDateTime localDateTime);
}
