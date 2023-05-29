package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    @NotNull(message = "ID вещи не может быть пустым.")
    private Long itemId;
    @NotNull
    @FutureOrPresent(message = "Значение START не может быть в прошлом.")
    private LocalDateTime start;
    @NotNull
    @Future(message = "Значение END не может быть в прошлом.")
    private LocalDateTime end;
    private Status status;
    private User booker;
    private Item item;
}
