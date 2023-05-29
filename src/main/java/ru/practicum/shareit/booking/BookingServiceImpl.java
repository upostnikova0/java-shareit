package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;
import static ru.practicum.shareit.booking.Status.*;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(@Qualifier("bookingRepository") BookingRepository bookingRepository,
                              UserService userService,
                              @Qualifier("itemRepository") ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemRepository = itemRepository;
    }

    @Transactional
    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        User booker = toUser(userService.getUser(userId));
        isBookingValid(bookingDto);
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", bookingDto.getItemId()))
        );

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Владелец вещи пытается забронировать собственную вещь.");
            throw new BookingNotFoundException("Нельзя забронировать собственную вещь.");
        }

        Booking booking = toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(WAITING);
        bookingRepository.save(booking);

        log.info(String.format("Новое бронирование в базе: id # %d.", booking.getId()));
        return toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto updateBookingStatus(Long userId, Long id, Boolean approved) {
        userService.getUser(userId);
        Booking booking = BookingMapper.toBooking(getById(userId, id));

        if (userId.equals(booking.getItem().getOwner().getId())) {
            if (approved) {
                if (booking.getStatus().equals(WAITING)) {
                    booking.setStatus(Status.APPROVED);
                } else {
                    throw new ValidationException("Бронирование уже одобрено.");
                }
            } else {
                if (booking.getStatus().equals(WAITING)) {
                    booking.setStatus(REJECTED);
                } else {
                    throw new ValidationException("Бронирование уже отклонено.");
                }
            }
        } else {
            throw new BookingNotFoundException("Действие может совершить только владелец вещи.");
        }

        bookingRepository.save(booking);
        return toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getById(Long userId, Long id) {
        User user = toUser(userService.getUser(userId));
        Optional<Booking> bookingOptional = bookingRepository.findById(id);

        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();

            if ((user.getId().equals(booking.getBooker().getId())) || (user.getId().equals(booking.getItem().getOwner().getId()))) {
                log.info("Найдено бронирование {}", bookingOptional.get());
                return toBookingDto(booking);
            } else {
                log.warn("Информация доступна только владельцу вещи и арендатору.");
                throw new BookingNotFoundException("Информация доступна только владельцу вещи и арендатору.");
            }
        } else {
            throw new BookingNotFoundException(String.format("Бронирование с ID %d не найдено.", id));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> getAllBookingsByUser(Long userId, State state) {
        User user = toUser(userService.getUser(userId));
        List<Booking> allBookings = new ArrayList<>();

        switch (state) {
            case ALL:
                allBookings.addAll(bookingRepository.findAllByBookerOrderByStartDesc(user));
                break;
            case CURRENT:
                allBookings.addAll(bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now()));
                break;
            case PAST:
                allBookings.addAll(bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now()));
                break;
            case FUTURE:
                allBookings.addAll(bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(user, LocalDateTime.now()));
                break;
            case WAITING:
                allBookings.addAll(bookingRepository.findAllByBookerAndStatusEqualsOrderByStartDesc(user, WAITING));
                break;
            case REJECTED:
                allBookings.addAll(bookingRepository.findAllByBookerAndStatusEqualsOrderByStartDesc(user, REJECTED));
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }

        return allBookings
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<BookingDto> getAllBookingsByOwner(Long userId, State state) {
        User user = toUser(userService.getUser(userId));
        List<Booking> allBookings = new ArrayList<>();

        switch (state) {
            case ALL:
                allBookings.addAll(bookingRepository.findAllByItemOwnerOrderByStartDesc(user));
                break;
            case CURRENT:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now()));
                break;
            case PAST:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now()));
                break;
            case FUTURE:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(user, LocalDateTime.now()));
                break;
            case WAITING:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEqualsOrderByStartDesc(user, WAITING));
                break;
            case REJECTED:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEqualsOrderByStartDesc(user, REJECTED));
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return allBookings
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Booking> findLastItem(Long itemId) {
        return bookingRepository.findLastItem(itemId, LocalDateTime.now());
    }

    @Override
    public Optional<Booking> findNextItem(Long itemId) {
        return bookingRepository.findNextItem(itemId, LocalDateTime.now());
    }

    @Override
    public List<Booking> findAllByBookerIdAndItemId(Long userId, Long itemId, Status status, LocalDateTime localDateTime) {
        return bookingRepository.findAllByBookerIdAndItemIdAndStatusEqualsAndEndIsBefore(userId, itemId, APPROVED, LocalDateTime.now());
    }

    private void isBookingValid(BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", bookingDto.getItemId()))
        );
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        if (!item.getAvailable()) {
            log.warn("Вещь недоступна для бронирования.");
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        if (end.isBefore(start) || end.equals(start)) {
            log.warn("Невалидные даты бронирования.");
            throw new ValidationException("Дата завершения бронирования должна быть позже даты начала бронирования.");
        }
    }
}
