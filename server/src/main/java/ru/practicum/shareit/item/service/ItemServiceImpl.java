package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Status.APPROVED;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

    public ItemServiceImpl(ItemRepository itemRepository,
                           CommentRepository commentRepository,
                           UserService userService,
                           BookingService bookingService,
                           ItemRequestService itemRequestService,
                           BookingMapper bookingMapper,
                           UserMapper userMapper,
                           CommentMapper commentMapper,
                           ItemMapper itemMapper,
                           ItemRequestMapper itemRequestMapper) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.itemRequestService = itemRequestService;
        this.bookingMapper = bookingMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
        this.itemMapper = itemMapper;
        this.itemRequestMapper = itemRequestMapper;
    }

    @Transactional
    @Override
    public ItemShortDto create(Long userId, ItemShortDto itemShortDto) {
        User user = userMapper.toUser(userService.getById(userId));
        Item item = itemMapper.toItem(itemShortDto);
        item.setOwner(user);

        if (itemShortDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestService.getById(itemShortDto.getRequestId(), userId));
            item.setRequest(itemRequest);
        }

        log.info("Добавлена вещь {}", item);
        itemRepository.save(item);
        return itemMapper.toItemShortDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Не найдена вещь с id: " + itemId)
        );

        userService.getById(userId);

        ItemDto itemDto = itemMapper.toItemDto(item);

        itemDto.setComments(commentRepository.findAllByItemId(itemId)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            Optional<Booking> lastBooking = bookingService.findLastItem(itemId);
            Optional<Booking> nextBooking = bookingService.findNextItem(itemId);

            if (lastBooking.isEmpty()) {
                itemDto.setLastBooking(null);
            } else {
                itemDto.setLastBooking(bookingMapper.toBookingShortDto(lastBooking.get()));
            }

            if (nextBooking.isEmpty()) {
                itemDto.setNextBooking(null);
            } else {
                itemDto.setNextBooking(bookingMapper.toBookingShortDto(nextBooking.get()));
            }
        }

        log.info("Найдена вещь {}", itemDto);
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAllItemsByUser(Long userId, Integer from, Integer size) {
        userService.getById(userId);
        List<ItemDto> allItemsByUser = itemRepository.findAllByOwnerId(userId, PageRequest.of(from / size, size))
                .stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(itemMapper::toItemDto)
                .peek(
                        itemDto -> {
                            Optional<Booking> lastBooking = bookingService.findLastItem(itemDto.getId());
                            Optional<Booking> nextBooking = bookingService.findNextItem(itemDto.getId());

                            if (lastBooking.isEmpty()) {
                                itemDto.setLastBooking(null);
                            } else {
                                itemDto.setLastBooking(bookingMapper.toBookingShortDto(lastBooking.get()));
                            }

                            if (nextBooking.isEmpty()) {
                                itemDto.setNextBooking(null);
                            } else {
                                itemDto.setNextBooking(bookingMapper.toBookingShortDto(nextBooking.get()));
                            }

                            itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId())
                                    .stream().map(commentMapper::toCommentDto).collect(Collectors.toList()));
                        })
                .collect(Collectors.toList());

        log.info("Найденные вещи {} пользователя с ID {}", allItemsByUser, userId);
        return allItemsByUser;
    }

    @Transactional
    @Override
    public ItemShortDto update(Long userId, ItemShortDto itemShortDto, Long itemId) {
        User user = userMapper.toUser(userService.getById(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Не найдена вещь с ID %d", itemId))
        );

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Не совпадают userId и ownerId.");
            throw new UserNotFoundException("Невозможно обновить вещь, т.к. она пренадлежит другому пользователю.");
        }

        if (itemShortDto.getName() == null) {
            itemShortDto.setName(item.getName());
        }

        if (itemShortDto.getDescription() == null) {
            itemShortDto.setDescription(item.getDescription());
        }

        if (itemShortDto.getAvailable() == null) {
            itemShortDto.setAvailable(item.getAvailable());
        }

        itemShortDto.setId(itemId);

        Item newItem = itemMapper.toItem(itemShortDto);
        newItem.setOwner(user);
        itemRepository.save(newItem);

        log.info("Обновлена вещь {}", newItem);
        return itemMapper.toItemShortDto(newItem);
    }

    @Transactional
    @Override
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemShortDto> searchItems(String text, Integer from, Integer size) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.searchItems(text, PageRequest.of(from / size, size))
                .stream()
                .map(itemMapper::toItemShortDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(long userId, CommentDto commentDto, long itemId) {
        User user = userMapper.toUser(userService.getById(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", itemId))
        );

        if (bookingService.findAllByBookerIdAndItemId(userId, itemId, APPROVED, LocalDateTime.now()).isEmpty()) {
            throw new BadRequestException("Комментарий может оставить только арендатор вещи после завершения аренды.");
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        return commentMapper.toCommentDto(comment);
    }
}
