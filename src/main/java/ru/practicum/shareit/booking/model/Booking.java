package ru.practicum.shareit.booking.model;

import lombok.*;
import lombok.experimental.NonFinal;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;
    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;
    @NonFinal
    @Enumerated(EnumType.STRING)
    private Status status;
}
