package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Status.*;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserService userService,
                              ItemRepository itemRepository,
                              BookingMapper bookingMapper,
                              UserMapper userMapper) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.bookingMapper = bookingMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public BookingDto create(Long userId, BookingShortDto bookingShortDto) {
        User booker = userMapper.toUser(userService.getById(userId));
        Item item = isBookingValid(bookingShortDto);

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Владелец вещи пытается забронировать собственную вещь.");
            throw new BookingNotFoundException("Нельзя забронировать собственную вещь.");
        }

        Booking booking = bookingMapper.toBooking(bookingShortDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(WAITING);
        bookingRepository.save(booking);

        log.info(String.format("Новое бронирование в базе: id # %d.", booking.getId()));
        return bookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto updateBookingStatus(Long userId, Long id, Boolean approved) {
        userService.getById(userId);
        Booking booking = bookingMapper.toBooking(getById(userId, id));

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
        return bookingMapper.toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getById(Long userId, Long id) {
        User user = userMapper.toUser(userService.getById(userId));
        Optional<Booking> bookingOptional = bookingRepository.findById(id);

        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();

            if ((user.getId().equals(booking.getBooker().getId())) || (user.getId().equals(booking.getItem().getOwner().getId()))) {
                log.info("Найдено бронирование {}", bookingOptional.get());
                return bookingMapper.toBookingDto(booking);
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
    public List<BookingDto> getAllBookingsByUser(Long userId, State state, Integer from, Integer size) {
        User user = userMapper.toUser(userService.getById(userId));
        List<Booking> allBookings = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(from / size, size);

        switch (state) {
            case ALL:
                allBookings.addAll(bookingRepository.findAllByBookerOrderByStartDesc(user, pageRequest));
                break;
            case CURRENT:
                allBookings.addAll(bookingRepository.findAllByBookerAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest));
                break;
            case PAST:
                allBookings.addAll(bookingRepository.findAllByBookerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest));
                break;
            case FUTURE:
                allBookings.addAll(bookingRepository.findAllByBookerAndStartAfterOrderByStartDesc(user, LocalDateTime.now(), pageRequest));
                break;
            case WAITING:
                allBookings.addAll(bookingRepository.findAllByBookerAndStatusEqualsOrderByStartDesc(user, WAITING, pageRequest));
                break;
            case REJECTED:
                allBookings.addAll(bookingRepository.findAllByBookerAndStatusEqualsOrderByStartDesc(user, REJECTED, pageRequest));
                break;
        }

        return allBookings
                .stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllBookingsByOwner(Long userId, State state, Integer from, Integer size) {
        User user = userMapper.toUser(userService.getById(userId));
        List<Booking> allBookings = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(from / size, size);

        switch (state) {
            case ALL:
                allBookings.addAll(bookingRepository.findAllByItemOwnerOrderByStartDesc(user, pageRequest));
                break;
            case CURRENT:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfterOrderByStartDesc(user, LocalDateTime.now(), LocalDateTime.now(), pageRequest));
                break;
            case PAST:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndEndBeforeOrderByStartDesc(user, LocalDateTime.now(), pageRequest));
                break;
            case FUTURE:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStartAfterOrderByStartDesc(user, LocalDateTime.now(), pageRequest));
                break;
            case WAITING:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEqualsOrderByStartDesc(user, WAITING, pageRequest));
                break;
            case REJECTED:
                allBookings.addAll(bookingRepository.findAllByItemOwnerAndStatusEqualsOrderByStartDesc(user, REJECTED, pageRequest));
                break;
        }
        return allBookings
                .stream()
                .map(bookingMapper::toBookingDto)
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

    private Item isBookingValid(BookingShortDto bookingShortDto) {
        Item item = itemRepository.findById(bookingShortDto.getItemId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", bookingShortDto.getItemId()))
        );
        LocalDateTime start = bookingShortDto.getStart();
        LocalDateTime end = bookingShortDto.getEnd();

        if (!item.getAvailable()) {
            log.warn("Вещь недоступна для бронирования.");
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        if (end.isBefore(start) || end.equals(start)) {
            log.warn("Невалидные даты бронирования.");
            throw new ValidationException("Дата завершения бронирования должна быть позже даты начала бронирования.");
        }

        return item;
    }
}
