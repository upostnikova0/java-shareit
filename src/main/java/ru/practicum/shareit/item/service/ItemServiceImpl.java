package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBookingShortDto;
import static ru.practicum.shareit.booking.Status.APPROVED;
import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;
import static ru.practicum.shareit.item.comment.CommentMapper.toComment;
import static ru.practicum.shareit.item.comment.CommentMapper.toCommentDto;
import static ru.practicum.shareit.user.UserMapper.toUser;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final BookingService bookingService;

    public ItemServiceImpl(@Qualifier("itemRepository") ItemRepository itemRepository,
                           @Qualifier("commentRepository") CommentRepository commentRepository,
                           UserService userService,
                           BookingService bookingService) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @Transactional
    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = toUser(userService.getUser(userId));
        Item item = itemRepository.save(toItem(itemDto, user));

        log.info("Вещь {} успешно добавлена.", item);
        return toItemDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Не найдена вещь с id: " + itemId)
        );

        userService.getUser(userId);

        ItemDto itemDto = toItemDto(item);

        itemDto.setComments(commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            Optional<Booking> lastBooking = bookingService.findLastItem(itemId);
            Optional<Booking> nextBooking = bookingService.findNextItem(itemId);

            if (lastBooking.isEmpty()) {
                itemDto.setLastBooking(null);
            } else {
                itemDto.setLastBooking(toBookingShortDto(lastBooking.get()));
            }

            if (nextBooking.isEmpty()) {
                itemDto.setNextBooking(null);
            } else {
                itemDto.setNextBooking(toBookingShortDto(nextBooking.get()));
            }
        }

        log.info("Найдена вещь {}", itemDto);
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> getAllItemsByUser(Long userId) {
        userService.getUser(userId);
        Collection<ItemDto> allItemsByUser = itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .peek(
                        itemDto -> {
                            Optional<Booking> lastBooking = bookingService.findLastItem(itemDto.getId());
                            Optional<Booking> nextBooking = bookingService.findNextItem(itemDto.getId());

                            if (lastBooking.isEmpty()) {
                                itemDto.setLastBooking(null);
                            } else {
                                itemDto.setLastBooking(toBookingShortDto(lastBooking.get()));
                            }

                            if (nextBooking.isEmpty()) {
                                itemDto.setNextBooking(null);
                            } else {
                                itemDto.setNextBooking(toBookingShortDto(nextBooking.get()));
                            }

                            itemDto.setComments(commentRepository.findAllByItemId(itemDto.getId())
                                    .stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
                        })
                .collect(Collectors.toList());

        log.info("Найденные вещи {} пользователя с ID {}", allItemsByUser, userId);
        return allItemsByUser;
    }

    @Transactional
    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        User user = toUser(userService.getUser(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Не найдена вещь с ID %d", itemId))
        );

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Не совпадают userId и ownerId.");
            throw new UserNotFoundException("Невозможно обновить вещь, т.к. она пренадлежит другому пользователю.");
        }

        if (itemDto.getName() == null) {
            itemDto.setName(item.getName());
        }

        if (itemDto.getDescription() == null) {
            itemDto.setDescription(item.getDescription());
        }

        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(item.getAvailable());
        }

        itemDto.setId(itemId);

        Item newItem = toItem(itemDto, user);
        itemRepository.save(newItem);

        log.info("Вещь {} успешно обновлена.", newItem);
        return toItemDto(newItem);
    }

    @Transactional
    @Override
    public void delete(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.searchItems(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(long userId, CommentDto commentDto, long itemId) {
        User user = toUser(userService.getUser(userId));
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена.", itemId))
        );

        if (bookingService.findAllByBookerIdAndItemId(userId, itemId, APPROVED, LocalDateTime.now()).isEmpty()) {
            throw new BadRequestException("Комментарий может оставить только арендатор вещи после завершения аренды.");
        }

        Comment comment = toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        commentRepository.save(comment);

        return toCommentDto(comment);
    }
}
