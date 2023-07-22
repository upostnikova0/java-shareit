package ru.practicum.shareit.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserService userService,
                                  ItemRepository itemRepository,
                                  ItemRequestMapper itemRequestMapper,
                                  ItemMapper itemMapper,
                                  UserMapper userMapper) {
        this.itemRequestRepository = itemRequestRepository;
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.itemRequestMapper = itemRequestMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    @Override
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = userMapper.toUser(userService.getById(userId));

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(itemRequest);

        log.info("Добавлен новый запрос {}", itemRequest);
        return itemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllRequestsByOwner(Long userId) {
        userService.getById(userId);

        List<ItemRequest> itemRequestList = itemRequestRepository.findAllByRequesterIdOrderByCreated(userId);

        if (itemRequestList.isEmpty()) {
            log.info("Список запросов владельца пуст.");
            return new ArrayList<>();
        } else {
            List<ItemRequestDto> itemRequestDtoList = itemRequestList.stream()
                    .map(itemRequestMapper::toItemRequestDto)
                    .peek(this::getItemResponseDto)
                    .collect(Collectors.toList());

            log.info("Список запросов владельца {}", itemRequestDtoList);
            return itemRequestDtoList;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        int pageNumber;
        if (from > 0 && size > 0) {
            pageNumber = from / size;
        } else if (from == 0 && size > 0) {
            pageNumber = 0;
        } else {
            throw new BadRequestException("Индекс первого элемента должен быть больше или равен нулю, а кол-во элементов должно быть больше нуля.");
        }

        User user = userMapper.toUser(userService.getById(userId));

        List<ItemRequestDto> itemRequestDtoList = itemRequestRepository.findAllByRequesterNotLikeOrderByCreatedAsc(user, PageRequest.of(pageNumber, size))
                .stream()
                .map(itemRequestMapper::toItemRequestDto)
                .peek(this::getItemResponseDto)
                .collect(Collectors.toList());

        log.info("Список запросов: {}", itemRequestDtoList);
        return itemRequestDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        userService.getById(userId);

        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new ItemNotFoundException("Невозможно найти запрос с id " + requestId)
        );

        ItemRequestDto itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);
        getItemResponseDto(itemRequestDto);

        log.info("Найден запрос {}", itemRequestDto);
        return itemRequestDto;
    }

    private void getItemResponseDto(ItemRequestDto itemRequestDto) {
        itemRequestDto.setItems(itemRepository.findAllByRequestId(itemRequestDto.getId())
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList()));
    }
}
