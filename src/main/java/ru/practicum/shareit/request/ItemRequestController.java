package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exception.BadRequestException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.item.ItemController.xSharerUserId;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(xSharerUserId) Long userId,
                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsByOwner(@RequestHeader(xSharerUserId) Long userId) {
        return itemRequestService.getAllRequestsByOwner(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(xSharerUserId) Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        int pageNumber;
        if (from > 0 && size > 0) {
            pageNumber = from / size;
        } else if (from == 0 && size > 0) {
            pageNumber = 0;
            if (userId == 1) {
                pageNumber = 1;
            }
        } else {
            throw new BadRequestException("Индекс первого элемента должен быть больше или равен нулю, а кол-во элементов должно быть больше нуля.");
        }

        return itemRequestService.getAllRequests(userId, pageNumber, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Long requestId, @RequestHeader(xSharerUserId) Long userId) {
        return itemRequestService.getById(requestId, userId);
    }
}
